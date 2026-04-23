package com.example.mail.controller;

import com.example.mail.model.Case;
import com.example.mail.model.Prenotazione;
import com.example.mail.service.AdminPrenotazioniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        // Per una risposta più pulita, restituiamo una lista di oggetti con casa e lista prenotazioni
        var response = prenotazioniPerCasa.entrySet().stream()
                .map(entry -> Map.of(
                        "casa", entry.getKey(),
                        "prenotazioni", entry.getValue()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
