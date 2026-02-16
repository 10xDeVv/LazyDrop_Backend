package com.lazydrop.modules.billing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(
        name = "stripe_webhook_events",
        uniqueConstraints = @UniqueConstraint(name = "ux_stripe_webhook_events_event_id", columnNames = "stripeEventId")
)
public class StripeWebhookEvent {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String stripeEventId;

    @Column(length = 255)
    private String type;

    private Boolean livemode;

    @Column(nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StripeWebhookStatus status;

    @Column(nullable = false)
    private int attemptCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(columnDefinition = "text")
    private String lastError;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(nullable = false, columnDefinition = "text")
    private String sigHeader;
}
