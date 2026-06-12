package com.example.mail.service;

import com.example.mail.EmailService;
import com.example.mail.model.Prenotazione;
import com.example.mail.repository.PrenotazioneRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.model.EventDataObjectDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class StripeWebhookService {
    private static final Logger log = LoggerFactory.getLogger(StripeWebhookService.class);
    private static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
    private static final String STATO_CAPARRA_PAGATA = "CAPARRA_PAGATA";
    private static final String ADMIN_SUBJECT_PREFIX = "Pagamento Stripe ricevuto - prenotazione ";
    private static final String GUEST_SUBJECT = "Conferma pagamento caparra - Torre Pali Vacanze";

    private final PrenotazioneRepository prenotazioneRepository;
    private final EmailService emailService;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${stripe.webhook-secret-test:}")
    private String webhookSecretTest;

    @Value("${stripe.notification-email:info@torrepalivacanze.it}")
    private String notificationEmail;

    @Value("${stripe.use-test-mode:false}")
    private boolean useTestMode;

    public StripeWebhookService(PrenotazioneRepository prenotazioneRepository, EmailService emailService) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String handleCheckoutSessionCompleted(String payload, String signatureHeader) throws SignatureVerificationException {
        String secret = getActiveWebhookSecret();
        if (secret == null || secret.isBlank()) {
            log.error("Webhook Stripe non configurato: secret vuoto (useTestMode={})", useTestMode);
            throw new IllegalStateException("Stripe webhook non configurato");
        }

        Event event = Webhook.constructEvent(payload, signatureHeader, secret);
        log.info("Webhook Stripe ricevuto: type={} id={}", event.getType(), event.getId());
        if (!CHECKOUT_SESSION_COMPLETED.equals(event.getType())) {
            return "ignored:" + event.getType();
        }

        Session session = extractSession(event);

        String prenotazioneIdValue = session.getMetadata() != null ? session.getMetadata().get("prenotazioneId") : null;
        if (prenotazioneIdValue == null || prenotazioneIdValue.isBlank()) {
            throw new IllegalStateException("prenotazioneId mancante nei metadata Stripe");
        }

        UUID prenotazioneId = UUID.fromString(prenotazioneIdValue);
        Prenotazione prenotazione = prenotazioneRepository.findById(prenotazioneId)
                .orElseThrow(() -> new IllegalStateException("Prenotazione non trovata"));

        if (STATO_CAPARRA_PAGATA.equalsIgnoreCase(prenotazione.getStato())) {
            log.info("Webhook Stripe già processato per prenotazione {}", prenotazione.getId());
            return "already-processed";
        }

        sendNotifications(prenotazione, session);

        prenotazione.setStato(STATO_CAPARRA_PAGATA);
        prenotazioneRepository.save(prenotazione);
        log.info("Webhook Stripe processato con successo per prenotazione {}", prenotazione.getId());

        return "processed";
    }

    private void sendNotifications(Prenotazione prenotazione, Session session) {
        emailService.sendSimpleMessage(
                notificationEmail,
                ADMIN_SUBJECT_PREFIX + prenotazione.getId(),
                buildNotificationBody(prenotazione, session)
        );

        String guestEmail = valueOrEmpty(prenotazione.getEmailOspite()).trim();
        if (!guestEmail.isBlank()) {
            emailService.sendSimpleMessage(
                    guestEmail,
                    GUEST_SUBJECT,
                    buildGuestConfirmationBody(prenotazione, session)
            );
        }
    }

    private String getActiveWebhookSecret() {
        if (useTestMode && webhookSecretTest != null && !webhookSecretTest.isBlank()) {
            return webhookSecretTest;
        }
        return webhookSecret;
    }

    private Session extractSession(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        // First try safe deserialization (preferred when API versions match).
        Session safeSession = deserializer.getObject()
                .filter(Session.class::isInstance)
                .map(Session.class::cast)
                .orElse(null);
        if (safeSession != null) {
            return safeSession;
        }

        // Fallback for API version mismatches between Stripe account and stripe-java models.
        try {
            StripeObject unsafe = deserializer.deserializeUnsafe();
            if (unsafe instanceof Session) {
                log.warn("Webhook Stripe deserializzato in modalita unsafe (eventId={}, type={})", event.getId(), event.getType());
                return (Session) unsafe;
            }
            throw new IllegalStateException("Oggetto webhook non e una Sessione checkout");
        } catch (EventDataObjectDeserializationException | RuntimeException ex) {
            throw new IllegalStateException("Sessione Stripe non disponibile nel webhook", ex);
        }
    }

    private String buildNotificationBody(Prenotazione prenotazione, Session session) {
        StringBuilder body = new StringBuilder();
        body.append("È stato ricevuto un pagamento Stripe per una prenotazione.\n\n");
        body.append("Prenotazione ID: ").append(prenotazione.getId()).append('\n');
        body.append("Casa ID: ").append(prenotazione.getCasaId()).append('\n');
        body.append("Ospite: ").append(valueOrEmpty(prenotazione.getOspiteNome())).append('\n');
        body.append("Email ospite: ").append(valueOrEmpty(prenotazione.getEmailOspite())).append('\n');
        body.append("Telefono ospite: ").append(valueOrEmpty(prenotazione.getTelefonoOspite())).append('\n');
        body.append("Check-in: ").append(prenotazione.getCheckIn()).append('\n');
        body.append("Check-out: ").append(prenotazione.getCheckOut()).append('\n');
        body.append("Stato: ").append(valueOrEmpty(prenotazione.getStato())).append('\n');
        body.append("Prezzo totale: ").append(formatMoney(prenotazione.getPrezzoTotale())).append('\n');
        body.append("Caparra: ").append(formatMoney(prenotazione.getCaparra())).append('\n');
        body.append("Stripe session: ").append(valueOrEmpty(session.getId())).append('\n');
        body.append("Payment intent: ").append(valueOrEmpty(session.getPaymentIntent())).append('\n');
        if (session.getCreated() != null) {
            body.append("Pagato il: ")
                    .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(Instant.ofEpochSecond(session.getCreated()).atOffset(java.time.ZoneOffset.UTC)))
                    .append('\n');
        }
        return body.toString();
    }

    private String buildGuestConfirmationBody(Prenotazione prenotazione, Session session) {
        StringBuilder body = new StringBuilder();
        body.append("Ciao ").append(valueOrEmpty(prenotazione.getOspiteNome())).append(",\n\n");
        body.append("abbiamo ricevuto correttamente il pagamento della caparra per la tua prenotazione.\n\n");
        body.append("Dettagli:\n");
        body.append("- Prenotazione ID: ").append(prenotazione.getId()).append('\n');
        body.append("- Check-in: ").append(prenotazione.getCheckIn()).append('\n');
        body.append("- Check-out: ").append(prenotazione.getCheckOut()).append('\n');
        body.append("- Caparra pagata: ").append(formatMoney(prenotazione.getCaparra())).append('\n');
        body.append("- Sessione Stripe: ").append(valueOrEmpty(session.getId())).append('\n');
        body.append("\nGrazie per aver scelto Torre Pali Vacanze.\n");
        body.append("Per assistenza rispondi a questa email o contattaci al +39 388 658 70 80.");
        return body.toString();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " EUR";
    }
}