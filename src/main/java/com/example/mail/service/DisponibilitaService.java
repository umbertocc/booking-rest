package com.example.mail.service;

import com.example.mail.model.Case;
import com.example.mail.model.PrezzoCasa;
import com.example.mail.repository.CaseRepository;
import com.example.mail.repository.PrenotazioneRepository;
import com.example.mail.repository.PrezzoCasaRepository;
import com.example.mail.dto.CasaDisponibileDTO;
import com.example.mail.dto.CalendarioDisponibilitaDTO;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DisponibilitaService {
    private static final BigDecimal SUPPLEMENTO_OSPITE_AGGIUNTIVO = BigDecimal.valueOf(30);
    private static final int OSPITI_INCLUSI_NEL_PREZZO_BASE = 2;
    private static final long MIN_STAY_NIGHTS = 3;
    private static final int DEFAULT_CALENDAR_DAYS = 540;
    private static final int MAX_CALENDAR_DAYS = 730;

    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private PrezzoCasaRepository prezzoCasaRepository;

        public List<CasaDisponibileDTO> getCaseDisponibili(LocalDate checkIn, LocalDate checkOut, Integer ospiti) {
        validateStayRange(checkIn, checkOut);
        // Trova tutte le case che hanno almeno una prenotazione in conflitto
        Set<Long> caseOccupate = prenotazioneRepository.findPrenotazioniConflittoPerTutteLeCase(checkIn, checkOut)
                .stream()
                .map(p -> p.getCasaId())
            .collect(Collectors.toSet());

        List<Case> caseDisponibili = caseRepository.findAll().stream()
            .filter(c -> !caseOccupate.contains(c.getId()))
            .filter(c -> ospiti == null || ospiti <= 0 || (c.getMax_ospiti() != null && c.getMax_ospiti() >= ospiti))
            .collect(Collectors.toList());

        // Calcola il prezzo totale per ogni casa disponibile
        return caseDisponibili.stream()
                .map(casa -> new CasaDisponibileDTO(
                        casa,
                calcolaPrezzoTotale(casa, checkIn, checkOut, ospiti)
                ))
                .collect(Collectors.toList());
    }

    public CalendarioDisponibilitaDTO getCalendarioDisponibilita(int giorni) {
        int giorniRichiesti = giorni > 0 ? Math.min(giorni, MAX_CALENDAR_DAYS) : DEFAULT_CALENDAR_DAYS;
        LocalDate oggi = LocalDate.now();
        LocalDate fineFinestra = oggi.plusDays(giorniRichiesti + MIN_STAY_NIGHTS);
        List<Case> caseList = caseRepository.findAll();
        List<com.example.mail.model.Prenotazione> prenotazioni =
                prenotazioneRepository.findPrenotazioniConflittoPerTutteLeCase(oggi, fineFinestra);
        List<String> checkInDisponibili = new ArrayList<>();

        for (LocalDate checkIn = oggi; checkIn.isBefore(oggi.plusDays(giorniRichiesti)); checkIn = checkIn.plusDays(1)) {
            final LocalDate checkInCorrente = checkIn;
            final LocalDate checkOutCorrente = checkInCorrente.plusDays(MIN_STAY_NIGHTS);
            boolean casaDisponibile = caseList.stream().anyMatch(casa ->
                    prenotazioni.stream()
                            .filter(prenotazione -> casa.getId().equals(prenotazione.getCasaId()))
                            .noneMatch(prenotazione ->
                        prenotazione.getCheckIn().isBefore(checkOutCorrente)
                            && prenotazione.getCheckOut().isAfter(checkInCorrente)));
            if (casaDisponibile) {
            checkInDisponibili.add(checkInCorrente.toString());
            }
        }

        return new CalendarioDisponibilitaDTO(giorniRichiesti, checkInDisponibili);
    }

        private void validateStayRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intervallo date non valido");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights < MIN_STAY_NIGHTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il soggiorno minimo e di 3 notti");
        }
    }

        private BigDecimal calcolaPrezzoTotale(Case casa, LocalDate checkIn, LocalDate checkOut, Integer ospiti) {
        List<PrezzoCasa> prezzi = prezzoCasaRepository.findByCasaId(casa.getId());
        BigDecimal totale = BigDecimal.ZERO;
        for (LocalDate data = checkIn; data.isBefore(checkOut); data = data.plusDays(1)) {
            final LocalDate giorno = data;
            PrezzoCasa prezzo = prezzi.stream()
                    .filter(p -> (p.getInizioPeriodo().compareTo(giorno) <= 0 && p.getFinePeriodo().compareTo(giorno) >= 0))
                    .findFirst()
                    .orElse(null);
            if (prezzo != null) {
                totale = totale.add(BigDecimal.valueOf(prezzo.getPrezzoNotte()));
            }
        }

        int ospitiEffettivi = ospiti != null ? ospiti : OSPITI_INCLUSI_NEL_PREZZO_BASE;
        int ospitiAggiuntivi = Math.max(0, ospitiEffettivi - OSPITI_INCLUSI_NEL_PREZZO_BASE);
        if (ospitiAggiuntivi > 0) {
            totale = totale.add(SUPPLEMENTO_OSPITE_AGGIUNTIVO.multiply(BigDecimal.valueOf(ospitiAggiuntivi)));
        }

        return totale;
    }
}
