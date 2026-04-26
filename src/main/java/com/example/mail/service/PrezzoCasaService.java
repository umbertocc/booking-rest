package com.example.mail.service;

import com.example.mail.model.Case;
import com.example.mail.repository.CaseRepository;
import com.example.mail.dto.PrezzoCasaDTO;
import com.example.mail.model.PrezzoCasa;
import com.example.mail.repository.PrezzoCasaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrezzoCasaService {

        @Autowired
        private CaseRepository caseRepository;
    @Autowired
    private PrezzoCasaRepository prezzoCasaRepository;

    public List<PrezzoCasaDTO> getPrezziCase() {
        List<PrezzoCasa> prezzi = prezzoCasaRepository.findAll();
        return prezzi.stream().map(prezzo -> {
            long giorni = ChronoUnit.DAYS.between(prezzo.getInizioPeriodo(), prezzo.getFinePeriodo());
            if (giorni < 1) giorni = 1; // almeno 1 giorno
            int prezzoTotale = prezzo.getPrezzoNotte() * (int) giorni;
            Long casaId = prezzo.getCasa() != null ? prezzo.getCasa().getId() : null;
            return new PrezzoCasaDTO(
                prezzo.getId(),
                casaId,
                prezzo.getInizioPeriodo(),
                prezzo.getFinePeriodo(),
                prezzo.getPrezzoNotte(),
                prezzoTotale
            );
        }).collect(Collectors.toList());
    }

    public List<PrezzoCasaDTO> getPrezziCaseByCasaId(Long casaId) {
        List<PrezzoCasa> prezzi = prezzoCasaRepository.findByCasaId(casaId);
        return prezzi.stream().map(prezzo -> {
            long giorni = ChronoUnit.DAYS.between(prezzo.getInizioPeriodo(), prezzo.getFinePeriodo());
            if (giorni < 1) giorni = 1; // almeno 1 giorno
            int prezzoTotale = prezzo.getPrezzoNotte() * (int) giorni;
            Long casaIdValue = prezzo.getCasa() != null ? prezzo.getCasa().getId() : null;
            return new PrezzoCasaDTO(
                prezzo.getId(),
                casaIdValue,
                prezzo.getInizioPeriodo(),
                prezzo.getFinePeriodo(),
                prezzo.getPrezzoNotte(),
                prezzoTotale
            );
        }).collect(Collectors.toList());
    }
    public void deletePrezzoCasa(Long id) {
        prezzoCasaRepository.deleteById(id);
    }

    public void updatePrezzoCasa(Long id, PrezzoCasaDTO dto) {
        PrezzoCasa prezzo = prezzoCasaRepository.findById(id).orElseThrow(() -> new RuntimeException("PrezzoCasa non trovato"));
        prezzo.setInizioPeriodo(dto.getDataInizio());
        prezzo.setFinePeriodo(dto.getDataFine());
        prezzo.setPrezzoTotale(dto.getPrezzoTotale());
        prezzo.setPrezzoNotte(dto.getPrezzoNotte());
        prezzoCasaRepository.save(prezzo);
    }

    public void addPrezzoCasa(PrezzoCasaDTO dto) {
        PrezzoCasa prezzo = new PrezzoCasa();
        prezzo.setInizioPeriodo(dto.getDataInizio());
        prezzo.setFinePeriodo(dto.getDataFine());
        prezzo.setPrezzoTotale(dto.getPrezzoTotale());
        prezzo.setPrezzoNotte(dto.getPrezzoNotte());
        if (dto.getCasaId() != null) {
            Case casa = caseRepository.findById(dto.getCasaId())
                .orElseThrow(() -> new RuntimeException("Casa non trovata con id: " + dto.getCasaId()));
            prezzo.setCasa(casa);
        }
        prezzoCasaRepository.save(prezzo);
    }
}
