package com.example.mail.service;

import com.example.mail.model.Case;
import com.example.mail.model.PrezzoCasa;
import com.example.mail.repository.CaseRepository;
import com.example.mail.repository.PrenotazioneRepository;
import com.example.mail.repository.PrezzoCasaRepository;
import com.example.mail.dto.CasaDisponibileDTO;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DisponibilitaService {
    private static final BigDecimal SUPPLEMENTO_OSPITE_AGGIUNTIVO = BigDecimal.valueOf(50);
    private static final int OSPITI_INCLUSI_NEL_PREZZO_BASE = 2;
    private static final long MIN_STAY_NIGHTS = 3;

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
