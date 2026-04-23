package com.example.mail.service;

import com.example.mail.model.Case;
import com.example.mail.model.PrezzoCasa;
import com.example.mail.repository.CaseRepository;
import com.example.mail.repository.PrenotazioneRepository;
import com.example.mail.repository.PrezzoCasaRepository;
import com.example.mail.dto.CasaDisponibileDTO;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisponibilitaService {
    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private PrezzoCasaRepository prezzoCasaRepository;

    public List<CasaDisponibileDTO> getCaseDisponibili(LocalDate checkIn, LocalDate checkOut) {
        // Trova tutte le case che hanno almeno una prenotazione in conflitto
        List<Long> caseOccupate = prenotazioneRepository.findPrenotazioniConflittoPerTutteLeCase(checkIn, checkOut)
                .stream()
                .map(p -> p.getCasaId())
                .collect(Collectors.toList());
        List<Case> caseDisponibili;
        if (caseOccupate.isEmpty()) {
            caseDisponibili = caseRepository.findAll();
        } else {
            caseDisponibili = caseRepository.findAll().stream()
                    .filter(c -> !caseOccupate.contains(c.getId()))
                    .collect(Collectors.toList());
        }
        // Calcola il prezzo totale per ogni casa disponibile
        return caseDisponibili.stream()
                .map(casa -> new CasaDisponibileDTO(
                        casa,
                        calcolaPrezzoTotale(casa, checkIn, checkOut)
                ))
                .collect(Collectors.toList());
    }

    private BigDecimal calcolaPrezzoTotale(Case casa, LocalDate checkIn, LocalDate checkOut) {
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
        return totale;
    }
}
