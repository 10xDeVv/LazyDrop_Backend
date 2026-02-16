package com.lazydrop.modules.billing.model;

public record CheckoutResponse(
    String sessionId,
    String sessionUrl,
    String status
) {
}
