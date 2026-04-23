package com.example.mail.controller;

import com.example.mail.model.Prenotazione;
import com.example.mail.repository.PrenotazioneRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prenotazioni")
public class PrenotazioneController {
    private final PrenotazioneRepository prenotazioneRepository;

    public PrenotazioneController(PrenotazioneRepository prenotazioneRepository) {
        this.prenotazioneRepository = prenotazioneRepository;
    }

    // Tutte le prenotazioni
    @GetMapping
    public List<Prenotazione> getAll() {
        return prenotazioneRepository.findAll();
    }

    // Prenotazioni per id casa
    @GetMapping("/casa/{casaId}")
    public List<Prenotazione> getByCasaId(@PathVariable Long casaId) {
        return prenotazioneRepository.findAll().stream()
                .filter(p -> p.getCasaId().equals(casaId))
                .toList();
    }
}
