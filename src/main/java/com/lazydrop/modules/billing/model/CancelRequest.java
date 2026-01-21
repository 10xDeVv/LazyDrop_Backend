package com.lazydrop.modules.billing.model;

public record CancelRequest(
        String stripeSubscriptionId
) {
}
