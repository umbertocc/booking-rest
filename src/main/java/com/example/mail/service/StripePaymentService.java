package com.example.mail.service;

import com.example.mail.model.Prenotazione;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StripePaymentService {
    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.success-url:http://localhost:3000/payment/success}")
    private String successUrl;

    @Value("${stripe.cancel-url:http://localhost:3000/payment/cancel}")
    private String cancelUrl;

    public StripeCheckoutSession createCheckoutSession(Prenotazione prenotazione) {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new AdminPrenotazioniService.PublicBookingException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "Stripe non configurato sul server"
            );
        }

        try {
            Stripe.apiKey = stripeSecretKey;

            long amountInCents = prenotazione.getCaparra()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .putMetadata("prenotazioneId", prenotazione.getId().toString())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("eur")
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Caparra prenotazione")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return new StripeCheckoutSession(session.getId(), session.getUrl());
        } catch (StripeException ex) {
            throw new AdminPrenotazioniService.PublicBookingException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "Errore creazione sessione pagamento"
            );
        }
    }

    public static class StripeCheckoutSession {
        private final String sessionId;
        private final String sessionUrl;

        public StripeCheckoutSession(String sessionId, String sessionUrl) {
            this.sessionId = sessionId;
            this.sessionUrl = sessionUrl;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getSessionUrl() {
            return sessionUrl;
        }
    }
}
