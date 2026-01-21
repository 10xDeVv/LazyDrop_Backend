package com.lazydrop.modules.billing.model;

import com.lazydrop.modules.subscription.model.SubscriptionPlan;
import lombok.Builder;

import java.time.Instant;

@Builder
public record CancellationResponse(
        String message,
        Instant scheduledAt,
        SubscriptionPlan currentPlan,
        Instant effectiveUntil,
        SubscriptionPlan newPlan
) {
}
