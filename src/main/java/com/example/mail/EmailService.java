package com.example.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final String THREAD_DOMAIN = "torrepalivacanze.it";

    @Autowired
    private JavaMailSender mailSender;
 
    public EmailService() {
        // Costruttore pubblico vuoto per Mockito
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        sendSimpleMessage(new String[] { to }, subject, text);
    }

    public void sendSimpleMessage(String[] to, String subject, String text) {
        sendSimpleMessage(to, subject, text, null);
    }

    public void sendSimpleMessage(String[] to, String subject, String text, String replyTo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("info@torrepalivacanze.it");
        if (replyTo != null && !replyTo.trim().isBlank()) {
            message.setReplyTo(replyTo.trim());
        }
        mailSender.send(message);
    }

    public void sendSimpleMessageWithThread(String to, String subject, String text, String threadKey) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("info@torrepalivacanze.it");
            helper.setText(text, false);

            String referenceId = buildThreadReferenceId(threadKey);
            if (!referenceId.isBlank()) {
                message.setHeader("References", referenceId);
                message.setHeader("In-Reply-To", referenceId);
            }

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Impossibile inviare email testuale con threading", ex);
        }
    }

    public void sendHtmlMessage(String to, String subject, String plainText, String htmlText) {
        sendHtmlMessage(to, subject, plainText, htmlText, null, null);
    }

    public void sendHtmlMessage(String to, String subject, String plainText, String htmlText, String threadKey) {
        sendHtmlMessage(to, subject, plainText, htmlText, threadKey, null);
    }

    public void sendHtmlMessage(String to, String subject, String plainText, String htmlText, String threadKey, String replyTo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("info@torrepalivacanze.it");
            if (replyTo != null && !replyTo.trim().isBlank()) {
                helper.setReplyTo(replyTo.trim());
            }
            helper.setText(plainText, htmlText);

            String referenceId = buildThreadReferenceId(threadKey);
            if (!referenceId.isBlank()) {
                message.setHeader("References", referenceId);
                message.setHeader("In-Reply-To", referenceId);
            }

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Impossibile inviare email HTML", ex);
        }
    }

    private static String buildThreadReferenceId(String threadKey) {
        String raw = threadKey == null ? "" : threadKey.trim().toLowerCase();
        if (raw.isBlank()) {
            return "";
        }

        String normalized = raw.replaceAll("[^a-z0-9._-]", "-");
        normalized = normalized.replaceAll("-+", "-").replaceAll("(^-|-$)", "");
        if (normalized.isBlank()) {
            return "";
        }

        return "<prv-" + normalized + "@" + THREAD_DOMAIN + ">";
    }
}
