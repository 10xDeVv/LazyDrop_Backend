package com.lazydrop.modules.billing.service;

import com.lazydrop.modules.subscription.model.PlanLimits;
import com.lazydrop.modules.subscription.model.SubscriptionPlan;
import org.springframework.stereotype.Component;

@Component
public class PlanLimitsResolver {
    
    public PlanLimits getLimitsForPlan(SubscriptionPlan plan){
        return switch (plan) {
            case GUEST -> new PlanLimits(
                    SubscriptionPlan.GUEST,
                    2,
                    10,
                    3,
                    25L * 1024 * 1024,
                    4,
                    0,
                    0
            );
            case FREE -> new PlanLimits(
                    SubscriptionPlan.FREE, 
                    3,
                    15, 
                    5, 
                    100L * 1024 * 1024,
                    4,
                    100,
                    300
            );
            case PLUS -> new PlanLimits(
                    SubscriptionPlan.PLUS, 
                    10,
                    60,
                    50,
                    1024L * 1024 * 1024,
                    10,
                    500,
                    500
            );
            case PRO -> new PlanLimits(
                    SubscriptionPlan.PRO,
                    15,
                    120,
                    75,
                    2048L * 1024 * 1024,
                    15,
                    2000,
                    500
            );
            default -> throw new IllegalArgumentException("Unknown SubscriptionPlan: " + plan);
        };
    }
}
