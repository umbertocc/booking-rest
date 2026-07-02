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
    @Autowired
    private JavaMailSender mailSender;
 
    public EmailService() {
        // Costruttore pubblico vuoto per Mockito
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        sendSimpleMessage(new String[] { to }, subject, text);
    }

    public void sendSimpleMessage(String[] to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("info@torrepalivacanze.it");
        mailSender.send(message);
    }

    public void sendHtmlMessage(String to, String subject, String plainText, String htmlText) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("info@torrepalivacanze.it");
            helper.setText(plainText, htmlText);
            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Impossibile inviare email HTML", ex);
        }
    }
}
