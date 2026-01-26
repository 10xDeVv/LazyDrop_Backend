package com.lazydrop.modules.subscription.repository;

import com.lazydrop.modules.subscription.model.Subscription;
import com.lazydrop.modules.subscription.model.SubscriptionPlan;
import com.lazydrop.modules.subscription.model.SubscriptionStatus;
import com.lazydrop.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByUser(User user);
    
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<Subscription> findByStatusAndPlanCodeNotAndCurrentPeriodEndLessThanEqual(
            SubscriptionStatus status,
            SubscriptionPlan planCode,
            Instant cutoff
    );
    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);

}
