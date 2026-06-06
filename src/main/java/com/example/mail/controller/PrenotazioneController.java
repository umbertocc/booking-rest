package com.example.mail.controller;

import com.example.mail.dto.PrenotazioneDTO;
import com.example.mail.model.Prenotazione;
import com.example.mail.repository.PrenotazioneRepository;
import com.example.mail.service.AdminPrenotazioniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prenotazioni")
public class PrenotazioneController {
    private final PrenotazioneRepository prenotazioneRepository;
    private final AdminPrenotazioniService adminPrenotazioniService;

    public PrenotazioneController(PrenotazioneRepository prenotazioneRepository, AdminPrenotazioniService adminPrenotazioniService) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.adminPrenotazioniService = adminPrenotazioniService;
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

    // Endpoint pubblico per creare una prenotazione senza token
    @PostMapping("/public")
    public ResponseEntity<?> createPrenotazionePublic(@RequestBody PrenotazioneDTO dto) {
        PrenotazioneDTO created = adminPrenotazioniService.createPrenotazione(dto);
        if (created == null) {
            return ResponseEntity.badRequest().body("Dati mancanti o non validi");
        }
        return ResponseEntity.status(201).body(created);
    }
}
