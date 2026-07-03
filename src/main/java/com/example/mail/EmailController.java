package com.example.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    private static final String THREAD_PREFIX = "PRV-";

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public String sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String text,
            @RequestParam(required = false) String html,
            @RequestParam(required = false) String threadKey
    ) {
        String normalizedThread = normalizeThreadKey(threadKey);
        boolean hasHtml = html != null && !html.trim().isBlank();
        if (normalizedThread.isBlank()) {
            if (hasHtml) {
                emailService.sendHtmlMessage(to, subject, text, html);
            } else {
                emailService.sendSimpleMessage(to, subject, text);
            }
        } else {
            String threadedSubject = withThreadToken(subject, normalizedThread);
            if (hasHtml) {
                emailService.sendHtmlMessage(to, threadedSubject, text, html, normalizedThread);
            } else {
                emailService.sendSimpleMessageWithThread(
                        to,
                        threadedSubject,
                        text,
                        normalizedThread
                );
            }
        }
        return "Email inviata!";
    }

    private static String normalizeThreadKey(String value) {
        String raw = value == null ? "" : value.trim().toLowerCase();
        if (raw.startsWith(THREAD_PREFIX.toLowerCase())) {
            raw = raw.substring(THREAD_PREFIX.length());
        }
        return raw;
    }

    private static String withThreadToken(String subject, String threadKey) {
        String safeSubject = subject == null ? "" : subject.trim();
        String token = "[" + THREAD_PREFIX + threadKey + "]";
        if (safeSubject.contains(token)) {
            return safeSubject;
        }
        return (safeSubject + " " + token).trim();
    }
}
