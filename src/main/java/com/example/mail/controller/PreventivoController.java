package com.example.mail.controller;

import com.example.mail.EmailService;
import com.example.mail.model.Preventivo;
import com.example.mail.repository.PreventivoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/preventivi")
public class PreventivoController {

    private final PreventivoRepository preventivoRepository;
    private final EmailService emailService;

    public PreventivoController(PreventivoRepository preventivoRepository, EmailService emailService) {
        this.preventivoRepository = preventivoRepository;
        this.emailService = emailService;
    }

    @PostMapping("/public")
    public ResponseEntity<?> createPublicPreventivo(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String appartamento,
            @RequestParam(required = false) String arrivo,
            @RequestParam(required = false) String partenza,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(required = false) Integer persone,
            @RequestParam(required = false) String preferenzaRicontatto,
            @RequestParam(required = false) String messaggio,
            @RequestParam(required = false, defaultValue = "web") String source
    ) {
        String nomeValue = safeTrim(nome);
        String emailValue = safeTrim(email).toLowerCase(Locale.ROOT);

        if (nomeValue.isBlank()) {
            return ResponseEntity.badRequest().body("Il nome e obbligatorio");
        }
        if (emailValue.isBlank()) {
            return ResponseEntity.badRequest().body("L'email e obbligatoria");
        }

        String checkInRaw = firstNotBlank(checkIn, arrivo);
        String checkOutRaw = firstNotBlank(checkOut, partenza);

        LocalDate checkInDate = parseOptionalDate(checkInRaw);
        LocalDate checkOutDate = parseOptionalDate(checkOutRaw);

        Preventivo preventivo = new Preventivo();
        preventivo.setCreatedAt(OffsetDateTime.now());
        preventivo.setNome(nomeValue);
        preventivo.setEmail(emailValue);
        preventivo.setTelefono(safeTrim(telefono));
        preventivo.setAppartamento(safeTrim(appartamento));
        preventivo.setCheckIn(checkInDate);
        preventivo.setCheckOut(checkOutDate);
        preventivo.setPersone(persone);
        preventivo.setPreferenzaRicontatto(safeTrim(preferenzaRicontatto));
        preventivo.setMessaggio(safeTrim(messaggio));
        preventivo.setSource(safeTrim(source));

        Preventivo saved = preventivoRepository.save(preventivo);

        String mailText = buildMailText(saved);
        emailService.sendSimpleMessage(
                "info@torrepalivacanze.it",
                "richiesta preventivo torre pali vacanze",
                mailText
        );

        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "status", "saved"
        ));
    }

    private static String buildMailText(Preventivo p) {
        StringBuilder text = new StringBuilder();
        text.append("Nome: ").append(nullToEmpty(p.getNome())).append('\n');
        text.append("Email: ").append(nullToEmpty(p.getEmail())).append('\n');
        text.append("Telefono: ").append(nullToEmpty(p.getTelefono())).append('\n');
        text.append("Appartamento: ").append(nullToEmpty(p.getAppartamento())).append('\n');
        text.append("Check-in: ").append(p.getCheckIn() != null ? p.getCheckIn() : "").append('\n');
        text.append("Check-out: ").append(p.getCheckOut() != null ? p.getCheckOut() : "").append('\n');
        text.append("Persone: ").append(p.getPersone() != null ? p.getPersone() : "").append('\n');
        text.append("Preferenza ricontatto: ").append(nullToEmpty(p.getPreferenzaRicontatto())).append('\n');
        text.append("Messaggio: ").append(nullToEmpty(p.getMessaggio())).append('\n');
        text.append("Source: ").append(nullToEmpty(p.getSource())).append('\n');
        text.append("Preventivo ID: ").append(p.getId());
        return text.toString();
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNotBlank(String primary, String secondary) {
        String p = safeTrim(primary);
        if (!p.isBlank()) return p;
        return safeTrim(secondary);
    }

    private static LocalDate parseOptionalDate(String value) {
        String raw = safeTrim(value);
        if (raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
