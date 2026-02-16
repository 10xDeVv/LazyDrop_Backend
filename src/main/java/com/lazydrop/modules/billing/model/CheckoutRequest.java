package com.lazydrop.modules.billing.model;

import com.lazydrop.modules.subscription.model.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull(message = "Plan is required")
        SubscriptionPlan plan
) {
}
