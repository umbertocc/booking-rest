package com.example.mail.controller;

import com.example.mail.model.Case;
import com.example.mail.model.Prenotazione;
import com.example.mail.service.AdminPrenotazioniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.mail.dto.PrenotazioneDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/admin/prenotazioni")
public class AdminPrenotazioniController {
    private final AdminPrenotazioniService adminPrenotazioniService;

    public AdminPrenotazioniController(AdminPrenotazioniService adminPrenotazioniService) {
        this.adminPrenotazioniService = adminPrenotazioniService;
    }

    @GetMapping
    public ResponseEntity<?> getPrenotazioniPerCasa() {
        Map<Case, List<Prenotazione>> prenotazioniPerCasa = adminPrenotazioniService.getPrenotazioniPerCasa();
        var response = prenotazioniPerCasa.entrySet().stream()
            .map(entry -> {
                Map<String, Object> casaMap = new java.util.HashMap<>();
                casaMap.put("casa", entry.getKey());
                List<Map<String, Object>> prenotazioniList = entry.getValue().stream().map(p -> {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", p.getId());
                m.put("ospiteNome", p.getOspiteNome());
                m.put("checkIn", p.getCheckIn());
                m.put("checkOut", p.getCheckOut());
                m.put("emailOspite", p.getEmailOspite());
                m.put("note", p.getNote());
                m.put("telefonoOspite", p.getTelefonoOspite());
                m.put("prezzoTotale", p.getPrezzoTotale());
                m.put("caparra", p.getCaparra());
                m.put("createdAt", p.getCreatedAt());
                return m;
                }).collect(Collectors.toList());
                casaMap.put("prenotazioni", prenotazioniList);
                return casaMap;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePrenotazione(@PathVariable("id") java.util.UUID id, @RequestBody PrenotazioneDTO dto) {
        PrenotazioneDTO updated = adminPrenotazioniService.updatePrenotazione(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePrenotazione(@PathVariable("id") java.util.UUID id) {
        boolean deleted = adminPrenotazioniService.deletePrenotazione(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

        @PostMapping
    public ResponseEntity<?> createPrenotazione(@RequestBody PrenotazioneDTO dto) {
        PrenotazioneDTO created = adminPrenotazioniService.createPrenotazione(dto);
        if (created == null) {
            return ResponseEntity.badRequest().body("Dati mancanti o non validi");
        }
        return ResponseEntity.status(201).body(created); 
    }
}
