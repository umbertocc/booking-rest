package com.example.mail.service;

import com.example.mail.dto.PrenotazioneDTO;
import com.example.mail.model.Case;
import com.example.mail.model.Prenotazione;
import com.example.mail.model.PrezzoCasa;
import com.example.mail.repository.CaseRepository;
import com.example.mail.repository.PrenotazioneRepository;
import com.example.mail.repository.PrezzoCasaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminPrenotazioniService {
    private static final BigDecimal CAPARRA_PERCENTUALE = new BigDecimal("0.20");
    private static final BigDecimal SUPPLEMENTO_OSPITE_AGGIUNTIVO = BigDecimal.valueOf(50);
    private static final int OSPITI_INCLUSI_NEL_PREZZO_BASE = 2;
    private static final String STATO_IN_ATTESA_CAPARRA = "IN_ATTESA_CAPARRA";

    private final PrenotazioneRepository prenotazioneRepository;
    private final CaseRepository caseRepository;
    private final PrezzoCasaRepository prezzoCasaRepository;
    private final StripePaymentService stripePaymentService;

    public AdminPrenotazioniService(
            PrenotazioneRepository prenotazioneRepository,
            CaseRepository caseRepository,
            PrezzoCasaRepository prezzoCasaRepository,
            StripePaymentService stripePaymentService
    ) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.caseRepository = caseRepository;
        this.prezzoCasaRepository = prezzoCasaRepository;
        this.stripePaymentService = stripePaymentService;
    }

    public Map<Case, List<Prenotazione>> getPrenotazioniPerCasa() {
        List<Case> caseList = caseRepository.findAll();
        List<Prenotazione> prenotazioni = prenotazioneRepository.findAll();
        Map<Long, Case> caseMap = caseList.stream().collect(Collectors.toMap(Case::getId, c -> c));
        Map<Case, List<Prenotazione>> result = new LinkedHashMap<>();
        for (Case c : caseList) {
            result.put(c, new ArrayList<>());
        }
        for (Prenotazione p : prenotazioni) {
            Case c = caseMap.get(p.getCasaId());
            if (c != null) {
                result.get(c).add(p);
            }
        }
        return result;
    }

        @Transactional
        public PrenotazioneDTO updatePrenotazione(java.util.UUID id, PrenotazioneDTO dto) {
            Optional<Prenotazione> opt = prenotazioneRepository.findById(id);
            if (opt.isEmpty()) return null;
            Prenotazione p = opt.get();
            // Aggiorna solo i campi non null del DTO
            p.setOspiteNome(dto.getOspiteNome());
            p.setCheckIn(dto.getCheckIn());
            p.setCheckOut(dto.getCheckOut());
            p.setEmailOspite(dto.getEmailOspite());
            p.setTelefonoOspite(dto.getTelefonoOspite());
            p.setNumOspiti(dto.getNumOspiti());
            p.setStato(dto.getStato());
            p.setPrezzoTotale(dto.getPrezzoTotale());
            p.setCaparra(dto.getCaparra());
            p.setNote(dto.getNote());
            // Non aggiorniamo id, casaId, createdAt
            prenotazioneRepository.save(p);
            PrenotazioneDTO out = new PrenotazioneDTO();
            out.setId(p.getId());
            out.setOspiteNome(p.getOspiteNome());
            out.setCheckIn(p.getCheckIn());
            out.setCheckOut(p.getCheckOut());
            out.setEmailOspite(p.getEmailOspite());
            out.setNote(p.getNote());
            out.setTelefonoOspite(p.getTelefonoOspite());
            out.setNumOspiti(p.getNumOspiti());
            out.setStato(p.getStato());
            out.setPrezzoTotale(p.getPrezzoTotale());
            out.setCaparra(p.getCaparra());
            out.setCreatedAt(p.getCreatedAt());
            return out;
        }

    @Transactional
    public boolean deletePrenotazione(java.util.UUID id) {
        if (prenotazioneRepository.existsById(id)) {
            prenotazioneRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public PrenotazioneDTO createPrenotazione(com.example.mail.dto.PrenotazioneDTO dto) {
        // Verifica campi obbligatori
        if (dto.getOspiteNome() == null || dto.getCheckIn() == null || dto.getCheckOut() == null || dto.getCasaId() == null) {
            return null;
        }
        Prenotazione p = new Prenotazione();
        p.setOspiteNome(dto.getOspiteNome());
        p.setCheckIn(dto.getCheckIn());
        p.setCheckOut(dto.getCheckOut());
        p.setEmailOspite(dto.getEmailOspite());
        p.setTelefonoOspite(dto.getTelefonoOspite());
        p.setNumOspiti(dto.getNumOspiti());
        p.setStato(dto.getStato());
        p.setPrezzoTotale(dto.getPrezzoTotale());
        p.setCaparra(dto.getCaparra());
        p.setNote(dto.getNote());
        p.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : java.time.OffsetDateTime.now());
        p.setCasaId(dto.getCasaId());
        prenotazioneRepository.save(p);
        PrenotazioneDTO out = new PrenotazioneDTO();
        out.setId(p.getId());
        out.setOspiteNome(p.getOspiteNome());
        out.setCheckIn(p.getCheckIn());
        out.setCheckOut(p.getCheckOut());
        out.setEmailOspite(p.getEmailOspite());
        out.setTelefonoOspite(p.getTelefonoOspite());
        out.setNumOspiti(p.getNumOspiti());
        out.setStato(p.getStato());
        out.setPrezzoTotale(p.getPrezzoTotale());
        out.setCaparra(p.getCaparra());
        out.setNote(p.getNote());
        out.setCreatedAt(p.getCreatedAt());
        out.setCasaId(p.getCasaId());
        return out;
    }

    @Transactional
    public PrenotazioneDTO createPrenotazionePublicSecure(PrenotazioneDTO dto) {
        validatePublicPrenotazioneInput(dto);

        Case casa = caseRepository.findByIdForUpdate(dto.getCasaId())
                .orElseThrow(() -> new PublicBookingException(HttpStatus.BAD_REQUEST, "Casa non trovata"));

        if (casa.getMax_ospiti() == null || dto.getNumOspiti() > casa.getMax_ospiti()) {
            throw new PublicBookingException(HttpStatus.BAD_REQUEST, "Numero ospiti superiore alla capienza della casa");
        }

        if (prenotazioneRepository.existsConflitto(dto.getCasaId(), dto.getCheckIn(), dto.getCheckOut())) {
            throw new PublicBookingException(HttpStatus.CONFLICT, "Casa non disponibile nel periodo richiesto");
        }

        BigDecimal prezzoTotaleCalcolato = calcolaPrezzoTotaleServerSide(casa.getId(), dto.getCheckIn(), dto.getCheckOut(), dto.getNumOspiti());

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setOspiteNome(dto.getOspiteNome());
        prenotazione.setCheckIn(dto.getCheckIn());
        prenotazione.setCheckOut(dto.getCheckOut());
        prenotazione.setEmailOspite(dto.getEmailOspite());
        prenotazione.setTelefonoOspite(dto.getTelefonoOspite());
        prenotazione.setNumOspiti(dto.getNumOspiti());
        prenotazione.setStato(STATO_IN_ATTESA_CAPARRA);
        prenotazione.setPrezzoTotale(prezzoTotaleCalcolato);
        prenotazione.setCaparra(calcolaCaparraServerSide(prezzoTotaleCalcolato));
        prenotazione.setNote(dto.getNote());
        prenotazione.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : OffsetDateTime.now());
        prenotazione.setCasaId(dto.getCasaId());

        prenotazioneRepository.save(prenotazione);
        return toDto(prenotazione);
    }

    private void validatePublicPrenotazioneInput(PrenotazioneDTO dto) {
        if (dto == null) {
            throw new PublicBookingException(HttpStatus.BAD_REQUEST, "Payload prenotazione mancante");
        }
        if (dto.getOspiteNome() == null || dto.getOspiteNome().isBlank()) {
            throw new PublicBookingException(HttpStatus.BAD_REQUEST, "Nome ospite obbligatorio");
        }
        if (dto.getCasaId() == null) {
            throw new PublicBookingException(HttpStatus.BAD_REQUEST, "CasaId obbligatorio");
        }
        if (dto.getCheckIn() == null || dto.getCheckOut() == null) {
            throw new PublicBookingException(HttpStatus.BAD_REQUEST, "Check-in e check-out obbligatori");
        }
        if (!dto.getCheckIn().isBefore(dto.getCheckOut())) {
            throw new PublicBookingException(HttpStatus.BAD_REQUEST, "Intervallo date non valido");
        }
        if (dto.getNumOspiti() == null || dto.getNumOspiti() <= 0) {
            throw new PublicBookingException(HttpStatus.BAD_REQUEST, "Numero ospiti non valido");
        }
    }

    private BigDecimal calcolaPrezzoTotaleServerSide(Long casaId, LocalDate checkIn, LocalDate checkOut, Integer numOspiti) {
        List<PrezzoCasa> prezziCasa = prezzoCasaRepository.findByCasaId(casaId);
        if (prezziCasa.isEmpty()) {
            throw new PublicBookingException(HttpStatus.BAD_REQUEST, "Prezzi non configurati per la casa selezionata");
        }

        BigDecimal totale = BigDecimal.ZERO;
        for (LocalDate giorno = checkIn; giorno.isBefore(checkOut); giorno = giorno.plusDays(1)) {
            final LocalDate giornoCorrente = giorno;
            Optional<PrezzoCasa> prezzoGiornaliero = prezziCasa.stream()
                .filter(p -> !giornoCorrente.isBefore(p.getInizioPeriodo()) && !giornoCorrente.isAfter(p.getFinePeriodo()))
                    .findFirst();

            if (prezzoGiornaliero.isEmpty()) {
                throw new PublicBookingException(HttpStatus.BAD_REQUEST, "Prezzo non configurato per tutte le notti richieste");
            }

            totale = totale.add(BigDecimal.valueOf(prezzoGiornaliero.get().getPrezzoNotte()));
        }

        int ospitiEffettivi = numOspiti != null ? numOspiti : OSPITI_INCLUSI_NEL_PREZZO_BASE;
        int ospitiAggiuntivi = Math.max(0, ospitiEffettivi - OSPITI_INCLUSI_NEL_PREZZO_BASE);
        if (ospitiAggiuntivi > 0) {
            totale = totale.add(SUPPLEMENTO_OSPITE_AGGIUNTIVO.multiply(BigDecimal.valueOf(ospitiAggiuntivi)));
        }

        return totale;
    }

    private BigDecimal calcolaCaparraServerSide(BigDecimal prezzoTotale) {
        return prezzoTotale.multiply(CAPARRA_PERCENTUALE).setScale(2, RoundingMode.HALF_UP);
    }

    public PublicPaymentSessionResponse createPublicPaymentSession(UUID prenotazioneId) {
        Prenotazione prenotazione = prenotazioneRepository.findById(prenotazioneId)
                .orElseThrow(() -> new PublicBookingException(HttpStatus.NOT_FOUND, "Prenotazione non trovata"));

        if (prenotazione.getCaparra() == null && prenotazione.getPrezzoTotale() != null) {
            prenotazione.setCaparra(calcolaCaparraServerSide(prenotazione.getPrezzoTotale()));
            if (prenotazione.getStato() == null || prenotazione.getStato().isBlank()) {
                prenotazione.setStato(STATO_IN_ATTESA_CAPARRA);
            }
            prenotazioneRepository.save(prenotazione);
        }

        if (prenotazione.getCaparra() == null) {
            throw new PublicBookingException(HttpStatus.BAD_REQUEST, "Caparra non disponibile per la prenotazione");
        }

        StripePaymentService.StripeCheckoutSession checkoutSession =
            stripePaymentService.createCheckoutSession(prenotazione);

        return new PublicPaymentSessionResponse(
                prenotazione.getId(),
                prenotazione.getCaparra(),
            checkoutSession.getSessionId(),
            checkoutSession.getSessionUrl(),
            STATO_IN_ATTESA_CAPARRA
        );
    }

    private PrenotazioneDTO toDto(Prenotazione prenotazione) {
        PrenotazioneDTO out = new PrenotazioneDTO();
        out.setId(prenotazione.getId());
        out.setOspiteNome(prenotazione.getOspiteNome());
        out.setCheckIn(prenotazione.getCheckIn());
        out.setCheckOut(prenotazione.getCheckOut());
        out.setEmailOspite(prenotazione.getEmailOspite());
        out.setTelefonoOspite(prenotazione.getTelefonoOspite());
        out.setNumOspiti(prenotazione.getNumOspiti());
        out.setStato(prenotazione.getStato());
        out.setPrezzoTotale(prenotazione.getPrezzoTotale());
        out.setCaparra(prenotazione.getCaparra());
        out.setNote(prenotazione.getNote());
        out.setCreatedAt(prenotazione.getCreatedAt());
        out.setCasaId(prenotazione.getCasaId());
        return out;
    }

    public static class PublicBookingException extends RuntimeException {
        private final HttpStatus status;

        public PublicBookingException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }

    public static class PublicPaymentSessionResponse {
        private final UUID prenotazioneId;
        private final BigDecimal importoCaparra;
        private final String paymentSessionId;
        private final String paymentUrl;
        private final String stato;

        public PublicPaymentSessionResponse(
                UUID prenotazioneId,
                BigDecimal importoCaparra,
                String paymentSessionId,
                String paymentUrl,
                String stato
        ) {
            this.prenotazioneId = prenotazioneId;
            this.importoCaparra = importoCaparra;
            this.paymentSessionId = paymentSessionId;
            this.paymentUrl = paymentUrl;
            this.stato = stato;
        }

        public UUID getPrenotazioneId() {
            return prenotazioneId;
        }

        public BigDecimal getImportoCaparra() {
            return importoCaparra;
        }

        public String getPaymentSessionId() {
            return paymentSessionId;
        }

        public String getPaymentUrl() {
            return paymentUrl;
        }

        public String getStato() {
            return stato;
        }
    }
}
