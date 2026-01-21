package com.lazydrop.modules.subscription.mapper;

import com.lazydrop.modules.billing.model.CancellationResponse;
import com.lazydrop.modules.subscription.model.PlanLimits;
import com.lazydrop.modules.subscription.model.Subscription;
import com.lazydrop.modules.subscription.model.SubscriptionPlan;
import com.lazydrop.modules.subscription.model.SubscriptionResponse;

import java.time.Instant;

public class SubscriptionMapper {
    public static SubscriptionResponse toSubscriptionResponse(Subscription subscription, PlanLimits limits) {
        return SubscriptionResponse.builder()
                .planCode(subscription.getPlanCode())
                .status(subscription.getStatus())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .cancelAtPeriodEnd(subscription.isCancelAtPeriodEnd())
                .limits(limits)
                .build();
    }

    public static CancellationResponse toCancelResponse(Subscription subscription) {
        return CancellationResponse.builder()
                .message(subscription.isCancelAtPeriodEnd()
                        ? "Cancellation already scheduled."
                        : "Cancellation scheduled at period end.")
                .scheduledAt(Instant.now())
                .currentPlan(subscription.getPlanCode())
                .effectiveUntil(subscription.getCurrentPeriodEnd())
                .newPlan(SubscriptionPlan.FREE)
                .build();
    }

}
