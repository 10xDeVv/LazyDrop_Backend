package com.lazydrop.modules.subscription.scheduler;

import com.lazydrop.modules.subscription.model.Subscription;
import com.lazydrop.modules.subscription.model.SubscriptionPlan;
import com.lazydrop.modules.subscription.model.SubscriptionStatus;
import com.lazydrop.modules.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionDowngradeScheduler {

    private final SubscriptionRepository subscriptionRepository;

    // every 10 minutes (you can change to 5–15 mins)
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    @Transactional
    public void downgradeExpiredCanceledSubscriptions() {
        Instant now = Instant.now();

        List<Subscription> subs = subscriptionRepository
                .findByStatusAndPlanCodeNotAndCurrentPeriodEndLessThanEqual(
                        SubscriptionStatus.CANCELED,
                        SubscriptionPlan.FREE,
                        now
                );

        if (subs.isEmpty()) return;

        for (Subscription sub : subs) {
            sub.setPlanCode(SubscriptionPlan.FREE);
            // status can stay CANCELED; or you could set something like EXPIRED if you had it
        }

        subscriptionRepository.saveAll(subs);

        log.info("Downgraded {} expired canceled subscriptions at {}", subs.size(), now);
    }
}
