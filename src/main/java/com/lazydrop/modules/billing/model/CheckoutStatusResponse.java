package com.lazydrop.modules.billing.model;

import com.lazydrop.modules.subscription.model.SubscriptionPlan;

public record CheckoutStatusResponse(
        String state,
        SubscriptionPlan plan,
        String stripeSubscriptionId
) {}
