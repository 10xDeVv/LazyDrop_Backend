package com.lazydrop.modules.billing.service;

import com.lazydrop.common.exception.BadRequestException;
import com.lazydrop.config.StripeConfig;
import com.lazydrop.modules.subscription.model.Subscription;
import com.lazydrop.modules.subscription.model.SubscriptionPlan;
import com.lazydrop.modules.subscription.service.SubscriptionService;
import com.lazydrop.modules.user.model.User;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BillingService {

    private final SubscriptionService subscriptionService;
    private final StripeConfig stripeConfig;

    @Transactional
    public String getOrCreateStripeCustomer(User user) throws StripeException {
        if (user.isGuest()) {
            throw new BadRequestException("Guests cannot access billing. Create an account first.");
        }

        Subscription subscription = subscriptionService.getOrCreateSubscription(user);
        if (subscription.getStripeCustomerId() != null){
            return subscription.getStripeCustomerId();
        }

        CustomerCreateParams.Builder builder = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .putMetadata("app_user_id", user.getId().toString())
                .setName(user.getEmail());

        if (user.getSupabaseUserId() != null) {
            builder.putMetadata("supabase_user_id", user.getSupabaseUserId().toString());
        }

        Customer customer = Customer.create(builder.build());
        subscription.setStripeCustomerId(customer.getId());
        subscriptionService.updateSubscription(subscription);

        log.info("Created Stripe Customer {} for user {}", customer.getId(), user.getId());
        return customer.getId();
    }

    @Transactional
    public Session createCheckoutSession(User user, SubscriptionPlan plan)
            throws StripeException {
        String customerId = getOrCreateStripeCustomer(user);

        String priceId = switch (plan) {
            case FREE -> throw new BadRequestException("FREE has no checkout");
            case PLUS -> stripeConfig.getPlusPriceId();
            case PRO ->  stripeConfig.getProPriceId();
            default -> null;
        };

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                        .setCustomer(customerId)
                        .setSuccessUrl(stripeConfig.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                        .setCancelUrl(stripeConfig.getCancelUrl())
                        .putMetadata("app_user_id", user.getId().toString())
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(priceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .putMetadata("userId", user.getId().toString())
                        .putMetadata("plan_code", plan.name())
                        .setAllowPromotionCodes(true)
                        .build();
        Session session = Session.create(params);
        log.info("Checkout session {} created for user {} → {}",
                session.getId(), user.getId(), plan);

        return session;
    }

    @Transactional
    public com.stripe.model.billingportal.Session createBillingPortalSession(User user) throws StripeException {
        Subscription subscription = subscriptionService.getOrCreateSubscription(user);
        if (subscription.getStripeCustomerId() == null){
            throw new BadRequestException("User has no Stripe customer ID");
        }

        com.stripe.param.billingportal.SessionCreateParams params = com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(subscription.getStripeCustomerId())
                .setReturnUrl(stripeConfig.getSuccessUrl())
                .build();

        com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(params);
        log.info("Created billing portal session for user {}", user.getId());
        return session;
    }

    @Transactional
    public Subscription cancelSubscriptionAtPeriodEnd(User user) throws StripeException {
        Subscription subscription = subscriptionService.getOrCreateSubscription(user);

        if (subscription.getStripeSubscriptionId() == null) {
            throw new IllegalArgumentException("User has no active Stripe subscription");
        }

        com.stripe.model.Subscription stripeSubscription =
                com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId());

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true)
                .build();

        stripeSubscription.update(params);

        subscription.setCancelAtPeriodEnd(true);
        Subscription updated = subscriptionService.updateSubscription(subscription);

        log.info("Scheduled cancellation at period end for user {} (subscription: {})",
                user.getId(), subscription.getStripeSubscriptionId());

        return updated;
    }

    @Transactional
    public void reactivateSubscription(User user) throws StripeException {
        Subscription subscription = subscriptionService.getOrCreateSubscription(user);

        if (subscription.getStripeSubscriptionId() == null) {
            throw new IllegalArgumentException("User has no active Stripe subscription");
        }

        if (!subscription.isCancelAtPeriodEnd()) {
            throw new IllegalStateException("Subscription is not scheduled for cancellation");
        }

        com.stripe.model.Subscription stripeSubscription =
                com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId());

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(false)
                .build();

        stripeSubscription.update(params);

        subscription.setCancelAtPeriodEnd(false);
        subscriptionService.updateSubscription(subscription);

        log.info("Reactivated subscription for user {}", user.getId());
    }
}
