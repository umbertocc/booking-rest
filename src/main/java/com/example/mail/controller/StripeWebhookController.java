package com.example.mail.controller;

import com.example.mail.service.StripeWebhookService;
import com.stripe.exception.SignatureVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/stripe")
public class StripeWebhookController {
    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);
    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(StripeWebhookService stripeWebhookService) {
        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signatureHeader
    ) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("Webhook Stripe rifiutato: header Stripe-Signature mancante");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stripe-Signature mancante");
        }

        try {
            String result = stripeWebhookService.handleCheckoutSessionCompleted(payload, signatureHeader);
            return ResponseEntity.ok(result);
        } catch (SignatureVerificationException ex) {
            log.warn("Webhook Stripe firma non valida", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma Stripe non valida");
        } catch (IllegalStateException ex) {
            log.error("Webhook Stripe in stato non valido: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Errore interno webhook Stripe", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}