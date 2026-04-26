package com.example.mail.service;

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
    private PrezzoCasaRepository prezzoCasaRepository;

    public List<PrezzoCasaDTO> getPrezziCase() {
        List<PrezzoCasa> prezzi = prezzoCasaRepository.findAll();
        return prezzi.stream().map(prezzo -> {
            long giorni = ChronoUnit.DAYS.between(prezzo.getInizioPeriodo(), prezzo.getFinePeriodo());
            if (giorni < 1) giorni = 1; // almeno 1 giorno
            int prezzoTotale = prezzo.getPrezzoNotte() * (int) giorni;
            return new PrezzoCasaDTO(
                prezzo.getInizioPeriodo(),
                prezzo.getFinePeriodo(),
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
            return new PrezzoCasaDTO(
                prezzo.getInizioPeriodo(),
                prezzo.getFinePeriodo(),
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
        // Modifica sempre anche prezzoNotte
        if (dto instanceof com.example.mail.dto.PrezzoCasaDTO) {
            try {
                java.lang.reflect.Method m = dto.getClass().getMethod("getPrezzoNotte");
                Object prezzoNotteObj = m.invoke(dto);
                if (prezzoNotteObj != null) {
                    prezzo.setPrezzoNotte((Integer) prezzoNotteObj);
                }
            } catch (NoSuchMethodException ignored) {
                // Il metodo non esiste, ignora
            } catch (Exception e) {
                throw new RuntimeException("Errore nell'aggiornamento di prezzoNotte", e);
            }
        }
        prezzoCasaRepository.save(prezzo);
    }

    public void addPrezzoCasa(PrezzoCasaDTO dto) {
        PrezzoCasa prezzo = new PrezzoCasa();
        prezzo.setInizioPeriodo(dto.getDataInizio());
        prezzo.setFinePeriodo(dto.getDataFine());
        prezzo.setPrezzoTotale(dto.getPrezzoTotale());
        // prezzo.setCasa(...); // Da impostare se serve
        // prezzo.setPrezzoNotte(...); // Da impostare se serve
        prezzoCasaRepository.save(prezzo);
    }
}
