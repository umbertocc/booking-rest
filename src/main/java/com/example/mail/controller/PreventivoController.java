package com.example.mail.controller;

import com.example.mail.EmailService;
import com.example.mail.model.Case;
import com.example.mail.model.Preventivo;
import com.example.mail.repository.CaseRepository;
import com.example.mail.repository.PreventivoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/preventivi")
public class PreventivoController {
    private static final String SITE_BASE_URL = "https://torrepalivacanze.it";
    private static final String DEFAULT_CASE_LINK = SITE_BASE_URL + "/case-vacanze";
    private static final String THREAD_PREFIX = "PRV-";
    private static final String CONTACT_PHONE = "3804580135";
    private static final String DEFAULT_WHATSAPP_LINK = "https://wa.me/393804580135";

    private final PreventivoRepository preventivoRepository;
    private final CaseRepository caseRepository;
    private final EmailService emailService;

    public PreventivoController(PreventivoRepository preventivoRepository, CaseRepository caseRepository, EmailService emailService) {
        this.preventivoRepository = preventivoRepository;
        this.caseRepository = caseRepository;
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
                @RequestParam(required = false) String prezzo,
                @RequestParam(required = false) String prezzoTotale,
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
        String prezzoRaw = firstNotBlank(prezzo, prezzoTotale);

        LocalDate checkInDate = parseOptionalDate(checkInRaw);
        LocalDate checkOutDate = parseOptionalDate(checkOutRaw);
        BigDecimal prezzoValue = parseOptionalBigDecimal(prezzoRaw);

        Preventivo preventivo = new Preventivo();
        preventivo.setCreatedAt(OffsetDateTime.now());
        preventivo.setNome(nomeValue);
        preventivo.setEmail(emailValue);
        preventivo.setTelefono(safeTrim(telefono));
        preventivo.setAppartamento(safeTrim(appartamento));
        preventivo.setCheckIn(checkInDate);
        preventivo.setCheckOut(checkOutDate);
        preventivo.setPersone(persone);
        preventivo.setPrezzo(prezzoValue);
        preventivo.setPreferenzaRicontatto(safeTrim(preferenzaRicontatto));
        preventivo.setMessaggio(safeTrim(messaggio));
        preventivo.setSource(safeTrim(source));

        Preventivo saved = preventivoRepository.save(preventivo);

        String mailText = buildMailText(saved);
        emailService.sendSimpleMessage(
            new String[] { "info@torrepalivacanze.it"},
                "richiesta preventivo torre pali vacanze",
                mailText
        );

        Optional<Case> casaMatch = findCaseByAppartamento(saved.getAppartamento());
        String threadKey = safeTrim(saved.getId() != null ? saved.getId().toString() : "").toLowerCase(Locale.ROOT);
        String autoresponseBaseSubject = isBookingRequest(saved)
            ? "Richiesta di prenotazione ricevuta - Torre Pali Vacanze"
            : "Richiesta ricevuta - Torre Pali Vacanze";
        String autoresponseSubject = withThreadToken(autoresponseBaseSubject, threadKey);
        emailService.sendHtmlMessage(
            saved.getEmail(),
            autoresponseSubject,
            buildGuestAutoReplyText(saved, casaMatch.orElse(null)),
            buildGuestAutoReplyHtml(saved, casaMatch.orElse(null)),
            threadKey
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
        text.append("Prezzo: ").append(p.getPrezzo() != null ? p.getPrezzo() : "").append('\n');
        text.append("Preferenza ricontatto: ").append(nullToEmpty(p.getPreferenzaRicontatto())).append('\n');
        text.append("Messaggio: ").append(nullToEmpty(p.getMessaggio())).append('\n');
        text.append("Source: ").append(nullToEmpty(p.getSource())).append('\n');
        text.append("Preventivo ID: ").append(p.getId());
        return text.toString();
    }

    private Optional<Case> findCaseByAppartamento(String appartamento) {
        String value = safeTrim(appartamento);
        if (value.isBlank()) {
            return Optional.empty();
        }

        String normalizedValue = normalizeLookupKey(value);

        try {
            return caseRepository.findById(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return caseRepository.findAll().stream()
                    .filter(casa -> matchesCaseLookup(casa, value, normalizedValue))
                    .findFirst();
        }
    }

    private static boolean matchesCaseLookup(Case casa, String rawValue, String normalizedValue) {
        if (casa == null) {
            return false;
        }

        String nome = safeTrim(casa.getNome());
        String normalizedNome = normalizeLookupKey(nome);
        if (!normalizedNome.isBlank() && normalizedNome.equals(normalizedValue)) {
            return true;
        }

        if (!normalizedNome.isBlank() && (normalizedNome.contains(normalizedValue) || normalizedValue.contains(normalizedNome))) {
            return true;
        }

        String linkDettaglio = safeTrim(casa.getLink_dettaglio());
        String normalizedLink = normalizeLookupKey(linkDettaglio);
        if (!normalizedLink.isBlank() && (normalizedLink.contains(normalizedValue) || normalizedValue.contains(normalizedLink))) {
            return true;
        }

        String slugFromName = normalizeLookupKey(nome.replace(' ', '-'));
        return !slugFromName.isBlank() && (slugFromName.equals(normalizedValue) || slugFromName.contains(normalizedValue) || normalizedValue.contains(slugFromName));
    }

    private static String buildGuestAutoReplyText(Preventivo preventivo, Case casa) {
        boolean bookingRequest = isBookingRequest(preventivo);
        String struttura = casa != null ? safeTrim(casa.getNome()) : safeTrim(preventivo.getAppartamento());
        String indirizzo = casa != null ? safeTrim(casa.getIndirizzo()) : "";
        String linkStruttura = normalizeUrl(casa != null ? casa.getLink_dettaglio() : null, DEFAULT_CASE_LINK);
        String whatsappLink = resolveWhatsappLink(casa, bookingRequest);
        String prezzo = formatEuro(preventivo.getPrezzo());

        StringBuilder text = new StringBuilder();
        text.append("Ciao ").append(nullToEmpty(preventivo.getNome())).append(",\n\n");
        if (bookingRequest) {
            text.append("abbiamo ricevuto la tua richiesta di prenotazione.\n\n");
        } else {
            text.append("abbiamo ricevuto la tua richiesta di preventivo.\n\n");
        }
        text.append("Riepilogo richiesta:\n");
        text.append("- Struttura: ").append(structureOrFallback(struttura)).append('\n');
        text.append("- Indirizzo: ").append(indirizzo.isBlank() ? "-" : indirizzo).append('\n');
        text.append("- Check-in: ").append(formatDateIt(preventivo.getCheckIn())).append('\n');
        text.append("- Check-out: ").append(formatDateIt(preventivo.getCheckOut())).append('\n');
        text.append("- Ospiti: ").append(preventivo.getPersone() != null ? preventivo.getPersone() : "-").append("\n\n");
        if (!prezzo.isBlank()) {
            text.append("- Prezzo totale: ").append(prezzo).append("\n\n");
        }
        if (bookingRequest) {
            text.append("Ti contatteremo il prima possibile per la conferma e i prossimi passaggi.\n");
        } else {
            text.append("Ti invieremo il preventivo personalizzato il prima possibile.\n");
        }
        text.append("Cellulare: ").append(CONTACT_PHONE).append("\n");
        text.append("WhatsApp: ").append(whatsappLink).append("\n");
        text.append("Dettagli struttura: ").append(linkStruttura).append("\n\n");
        text.append("Grazie,\nTorre Pali Vacanze");
        return text.toString(); 
    }

    private static String buildGuestAutoReplyHtml(Preventivo preventivo, Case casa) {
        boolean bookingRequest = isBookingRequest(preventivo);
        String struttura = casa != null ? safeTrim(casa.getNome()) : safeTrim(preventivo.getAppartamento());
        String indirizzo = casa != null ? safeTrim(casa.getIndirizzo()) : "";
        String strutturaDisplay = escapeHtml(structureOrFallback(struttura));
        String indirizzoDisplay = escapeHtml(indirizzo.isBlank() ? "-" : indirizzo);
        String linkStruttura = normalizeUrl(casa != null ? casa.getLink_dettaglio() : null, DEFAULT_CASE_LINK);
        String whatsappLink = resolveWhatsappLink(casa, bookingRequest);
        String imageUrl = normalizeUrl(casa != null ? casa.getImmagine() : null, null);

        String checkIn = formatDateIt(preventivo.getCheckIn());
        String checkOut = formatDateIt(preventivo.getCheckOut());
        String ospiti = preventivo.getPersone() != null ? preventivo.getPersone().toString() : "-";
        String prezzo = formatEuro(preventivo.getPrezzo());
        String nome = escapeHtml(nullToEmpty(preventivo.getNome()));
        String intro = bookingRequest
                ? "Ciao " + nome + ", abbiamo ricevuto la tua richiesta di prenotazione e ti risponderemo al piu presto."
                : "Ciao " + nome + ", abbiamo ricevuto la tua richiesta di preventivo e ti risponderemo al piu presto.";
        String followUp = bookingRequest
                ? "Ti contatteremo il prima possibile per confermare disponibilita e prossimi passaggi."
                : "Ti invieremo il preventivo personalizzato appena possibile.";

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html lang=\"it\"><body style=\"margin:0;padding:0;background:#f5f7fb;font-family:Arial,sans-serif;color:#1f2937;\">")
            .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f5f7fb;padding:24px 0;\"><tr><td align=\"center\">")
            .append("<table role=\"presentation\" width=\"620\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:620px;width:100%;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e5e7eb;\">")
            .append("<tr><td style=\"padding:22px 24px 10px 24px;\"><h2 style=\"margin:0 0 8px 0;color:#0f766e;font-size:24px;\">Richiesta ricevuta</h2>")
            .append("<p style=\"margin:0;color:#475569;font-size:15px;line-height:1.6;\">").append(intro).append("</p></td></tr>");

        if (imageUrl != null && !imageUrl.isBlank()) {
            html.append("<tr><td style=\"padding:10px 24px 0 24px;\"><img src=\"")
                .append(escapeHtml(imageUrl))
                .append("\" alt=\"")
                .append(strutturaDisplay)
                .append("\" style=\"display:block;width:100%;max-height:280px;object-fit:cover;border-radius:10px;\"></td></tr>");
        }

        html.append("<tr><td style=\"padding:16px 24px 6px 24px;\">")
            .append("<p style=\"margin:0 0 10px 0;font-weight:700;color:#111827;font-size:16px;\">Riepilogo richiesta</p>")
            .append("<p style=\"margin:0 0 6px 0;color:#374151;font-size:14px;\"><strong>Struttura:</strong> ").append(strutturaDisplay).append("</p>")
            .append("<p style=\"margin:0 0 6px 0;color:#374151;font-size:14px;\"><strong>Indirizzo:</strong> ").append(indirizzoDisplay).append("</p>")
            .append("<p style=\"margin:0 0 6px 0;color:#374151;font-size:14px;\"><strong>Check-in:</strong> ").append(escapeHtml(checkIn)).append("</p>")
            .append("<p style=\"margin:0 0 6px 0;color:#374151;font-size:14px;\"><strong>Check-out:</strong> ").append(escapeHtml(checkOut)).append("</p>")
            .append("<p style=\"margin:0;color:#374151;font-size:14px;\"><strong>Ospiti:</strong> ").append(escapeHtml(ospiti)).append("</p>")
            .append(!prezzo.isBlank() ? "<p style=\"margin:6px 0 0 0;color:#166534;font-size:14px;\"><strong>Prezzo totale:</strong> " + escapeHtml(prezzo) + "</p>" : "")
            .append("</td></tr>")
            .append("<tr><td style=\"padding:18px 24px 24px 24px;\">")
            .append("<a href=\"").append(escapeHtml(linkStruttura)).append("\" target=\"_blank\" rel=\"noopener\" style=\"display:inline-block;background:#0f766e;color:#ffffff;text-decoration:none;font-weight:700;padding:12px 18px;border-radius:999px;font-size:14px;\">Vedi la struttura</a>")
            .append("<p style=\"margin:12px 0 0 0;color:#334155;font-size:14px;line-height:1.5;\"><strong>Cellulare:</strong> ").append(CONTACT_PHONE).append("</p>")
            .append("<p style=\"margin:14px 0 0 0;color:#64748b;font-size:13px;line-height:1.5;\">")
            .append(escapeHtml(followUp))
            .append(" Per urgenze puoi rispondere direttamente a questa email oppure scriverci su WhatsApp: ")
            .append("<a href=\"").append(escapeHtml(whatsappLink)).append("\" target=\"_blank\" rel=\"noopener\" style=\"color:#0f766e;text-decoration:underline;\">contattaci su WhatsApp</a>.")
            .append("</p>")
            .append("</td></tr></table></td></tr></table></body></html>");

        return html.toString();
    }

    private static String structureOrFallback(String struttura) {
        return struttura == null || struttura.isBlank() ? "La tua struttura selezionata" : struttura;
    }

    private static String normalizeUrl(String value, String fallback) {
        String raw = safeTrim(value);
        if (raw.isBlank()) return fallback;
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw;
        }
        if (raw.startsWith("/")) {
            return SITE_BASE_URL + raw;
        }
        return SITE_BASE_URL + "/" + raw;
    }

    private static String resolveWhatsappLink(Case casa, boolean bookingRequest) {
        if (bookingRequest && casa != null) {
            String bookingWhatsapp = normalizeUrl(casa.getLink_whatsapp_prenotazione(), null);
            if (bookingWhatsapp != null && !bookingWhatsapp.isBlank()) {
                return bookingWhatsapp;
            }
        }

        return normalizeUrl(casa != null ? casa.getLink_whatsapp() : null, DEFAULT_WHATSAPP_LINK);
    }

    private static String withThreadToken(String subject, String threadKey) {
        String safeSubject = safeTrim(subject);
        String key = safeTrim(threadKey).toLowerCase(Locale.ROOT);
        if (key.isBlank()) {
            return safeSubject;
        }

        String token = "[" + THREAD_PREFIX + key + "]";
        if (safeSubject.contains(token)) {
            return safeSubject;
        }
        return (safeSubject + " " + token).trim();
    }

    private static String escapeHtml(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNotBlank(String primary, String secondary) {
        String p = safeTrim(primary);
        if (!p.isBlank()) return p;
        return safeTrim(secondary);
    }

    private static String normalizeLookupKey(String value) {
        String normalized = safeTrim(value).toLowerCase(Locale.ROOT);
        normalized = normalized.replace(".html", "");
        normalized = normalized.replaceAll("https?://", "");
        normalized = normalized.replaceAll("/+$", "");
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-+|-+$", "");
        return normalized;
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

    private static BigDecimal parseOptionalBigDecimal(String value) {
        String raw = safeTrim(value);
        if (raw.isBlank()) return null;

        String sanitized = raw.replaceAll("[^0-9,.-]", "");
        if (sanitized.isBlank()) return null;

        // Esempi supportati: 1.250,50 / 1250.50 / 1250
        if (sanitized.contains(",") && sanitized.contains(".")) {
            sanitized = sanitized.replace(".", "").replace(",", ".");
        } else if (sanitized.contains(",")) {
            sanitized = sanitized.replace(",", ".");
        }

        try {
            return new BigDecimal(sanitized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBookingRequest(Preventivo preventivo) {
        String source = safeTrim(preventivo != null ? preventivo.getSource() : "").toLowerCase(Locale.ROOT);
        return source.startsWith("booking");
    }

    private static String formatDateIt(LocalDate value) {
        if (value == null) {
            return "-";
        }
        String iso = value.toString();
        String[] parts = iso.split("-");
        if (parts.length == 3) {
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        }
        return iso;
    }

    private static String formatEuro(BigDecimal value) {
        if (value == null) {
            return "";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.ITALY);
        return formatter.format(value);
    }
}
