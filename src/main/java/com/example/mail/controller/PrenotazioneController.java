package com.example.mail.controller;

import com.example.mail.dto.PrenotazioneDTO;
import com.example.mail.model.Prenotazione;
import com.example.mail.repository.PrenotazioneRepository;
import com.example.mail.service.AdminPrenotazioniService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/prenotazioni")
public class PrenotazioneController {
    private final PrenotazioneRepository prenotazioneRepository;
    private final AdminPrenotazioniService adminPrenotazioniService;
    private final Map<String, Deque<Instant>> requestHistoryByIp = new ConcurrentHashMap<>();
    private static final int PUBLIC_BOOKING_LIMIT = 5;
    private static final Duration PUBLIC_BOOKING_WINDOW = Duration.ofMinutes(15);

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
    public ResponseEntity<?> createPrenotazionePublic(@RequestBody PrenotazioneDTO dto, HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        if (!isAllowedByRateLimit(clientIp)) {
            return ResponseEntity.status(429).body("Troppe richieste. Riprova piu tardi.");
        }

        try {
            PrenotazioneDTO created = adminPrenotazioniService.createPrenotazionePublicSecure(dto);
            return ResponseEntity.status(201).body(created);
        } catch (AdminPrenotazioniService.PublicBookingException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isAllowedByRateLimit(String clientIp) {
        Instant now = Instant.now();
        Deque<Instant> history = requestHistoryByIp.computeIfAbsent(clientIp, ip -> new ArrayDeque<>());
        synchronized (history) {
            while (!history.isEmpty() && history.peekFirst().isBefore(now.minus(PUBLIC_BOOKING_WINDOW))) {
                history.pollFirst();
            }

            if (history.size() >= PUBLIC_BOOKING_LIMIT) {
                return false;
            }

            history.addLast(now);
            return true;
        }
    }
}
