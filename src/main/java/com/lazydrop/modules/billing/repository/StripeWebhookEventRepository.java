package com.lazydrop.modules.billing.repository;

import com.lazydrop.modules.billing.model.StripeWebhookEvent;
import com.lazydrop.modules.billing.model.StripeWebhookStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, UUID> {

    Optional<StripeWebhookEvent> findByStripeEventId(String stripeEventId);

    List<StripeWebhookEvent> findByStatusAndNextRetryAtLessThanEqualAndAttemptCountLessThanOrderByReceivedAtAsc(
            StripeWebhookStatus status,
            Instant now,
            int maxAttempts,
            Pageable pageable
    );

    // For lease-expired PROCESSING recovery
    List<StripeWebhookEvent> findByStatusAndNextRetryAtLessThanEqualOrderByReceivedAtAsc(
            StripeWebhookStatus status,
            Instant now,
            Pageable pageable
    );

    /**
     * ✅ Atomic claim. Only ONE node wins this update.
     * Also sets nextRetryAt = leaseUntil to act as a PROCESSING lease expiry.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update StripeWebhookEvent e
        set e.status = :processing,
            e.nextRetryAt = :leaseUntil
        where e.stripeEventId = :eventId
          and e.status in :claimable
    """)
    int claimWithLease(
            @Param("eventId") String eventId,
            @Param("processing") StripeWebhookStatus processing,
            @Param("claimable") List<StripeWebhookStatus> claimable,
            @Param("leaseUntil") Instant leaseUntil
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update StripeWebhookEvent e
        set e.status = :status,
            e.processedAt = :processedAt,
            e.attemptCount = :attemptCount,
            e.lastError = :lastError,
            e.nextRetryAt = :nextRetryAt
        where e.stripeEventId = :eventId
    """)
    int updateStatus(
            @Param("eventId") String eventId,
            @Param("status") StripeWebhookStatus status,
            @Param("processedAt") Instant processedAt,
            @Param("attemptCount") int attemptCount,
            @Param("lastError") String lastError,
            @Param("nextRetryAt") Instant nextRetryAt
    );
}
