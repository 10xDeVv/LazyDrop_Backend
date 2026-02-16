package com.lazydrop.modules.subscription.model;

import lombok.Builder;

import java.time.Instant;

@Builder
public record SubscriptionResponse(
        SubscriptionPlan planCode,
        SubscriptionStatus status,
        Instant currentPeriodEnd,
        boolean cancelAtPeriodEnd,
        PlanLimits limits) {
}
