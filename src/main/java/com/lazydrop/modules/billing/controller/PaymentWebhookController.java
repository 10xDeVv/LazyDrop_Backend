package com.lazydrop.modules.billing.controller;

import com.lazydrop.config.StripeConfig;
import com.lazydrop.modules.billing.service.PaymentWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
@Slf4j
public class PaymentWebhookController {
    private final StripeConfig stripeConfig;
    private final PaymentWebhookService paymentWebhookService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        String endpointSecret = stripeConfig.getWebhookSecret();

        Event event;
        try {
            event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    endpointSecret
            );
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");
        } catch (Exception e) {
            log.warn("Stripe webhook payload parse failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid payload");
        }

        try {
            paymentWebhookService.receiveAndStore(event, payload, sigHeader);
            // ✅ ACK once stored (Stripe cares about receipt, not that we finished processing)
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            // If this fails, it usually means DB save failed (bad — let Stripe retry)
            log.error("Stripe webhook failed BEFORE ACK (could not persist): eventId={} type={} err={}",
                    event.getId(), event.getType(), e.getMessage(), e);
            // 500 => Stripe will retry, which is what we want if we couldn't save it.
            return ResponseEntity.internalServerError().body("failed");
        }

    }
}


