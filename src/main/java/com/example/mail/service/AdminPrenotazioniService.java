package com.example.mail.service;

import com.example.mail.model.Case;
import com.example.mail.model.Prenotazione;
import com.example.mail.repository.CaseRepository;
import com.example.mail.repository.PrenotazioneRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminPrenotazioniService {
    private final PrenotazioneRepository prenotazioneRepository;
    private final CaseRepository caseRepository;

    public AdminPrenotazioniService(PrenotazioneRepository prenotazioneRepository, CaseRepository caseRepository) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.caseRepository = caseRepository;
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
}
