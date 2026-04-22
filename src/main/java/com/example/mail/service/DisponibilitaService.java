package com.example.mail.service;

import com.example.mail.model.Case;
import com.example.mail.repository.CaseRepository;
import com.example.mail.repository.PrenotazioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DisponibilitaService {
    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    public List<Case> getCaseDisponibili(LocalDate checkIn, LocalDate checkOut) {
        // Trova tutte le case che hanno almeno una prenotazione in conflitto
        List<Long> caseOccupate = prenotazioneRepository.findPrenotazioniConflittoPerTutteLeCase(checkIn, checkOut)
                .stream()
                .map(p -> p.getCasaId())
                .collect(Collectors.toList());
        // Restituisci solo le case che NON sono occupate
        if (caseOccupate.isEmpty()) {
            return caseRepository.findAll();
        } else {
            return caseRepository.findAll().stream()
                    .filter(c -> !caseOccupate.contains(c.getId()))
                    .collect(Collectors.toList());
        }
    }
}
