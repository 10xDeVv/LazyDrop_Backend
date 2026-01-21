package com.lazydrop.modules.subscription.controller;

import com.lazydrop.auth.IdentityResolver;
import com.lazydrop.modules.billing.model.*;
import com.lazydrop.modules.subscription.mapper.SubscriptionMapper;
import com.lazydrop.modules.subscription.model.PlanLimits;
import com.lazydrop.modules.subscription.model.SubscriptionPlan;
import com.lazydrop.security.UserPrincipal;
import com.lazydrop.modules.billing.service.BillingService;
import com.lazydrop.modules.subscription.model.Subscription;
import com.lazydrop.modules.subscription.model.SubscriptionResponse;
import com.lazydrop.modules.subscription.service.SubscriptionService;
import com.lazydrop.modules.user.model.User;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final BillingService billingService;
    private final IdentityResolver identityResolver;


    @GetMapping()
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response
            ){
        User user = identityResolver.resolve(principal, request, response);
        Subscription subscription = subscriptionService.getOrCreateSubscription(user);
        PlanLimits limits = subscriptionService.getLimitsForUser(user);

        return ResponseEntity.ok(SubscriptionMapper.toSubscriptionResponse(subscription, limits));
    }

    @GetMapping("/checkout/status")
    public ResponseEntity<CheckoutStatusResponse> getCheckoutStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String sessionId,
            HttpServletRequest req,
            HttpServletResponse res
    ) throws StripeException {

        User user = identityResolver.resolve(principal, req, res);
        Session session = Session.retrieve(sessionId);

        if (session.getMetadata() == null) {
            return ResponseEntity.ok(new CheckoutStatusResponse("PENDING", SubscriptionPlan.FREE, null));
        }

        String appUserId = session.getMetadata().get("app_user_id");
        if (appUserId == null || !appUserId.equals(user.getId().toString())) {
            return ResponseEntity.status(403)
                    .body(new CheckoutStatusResponse("PENDING", SubscriptionPlan.FREE, null));
        }

        boolean complete = "complete".equalsIgnoreCase(session.getStatus());
        if (!complete) {
            return ResponseEntity.ok(new CheckoutStatusResponse("PENDING", SubscriptionPlan.FREE, null));
        }

        String stripeSubId = session.getSubscription();
        if (stripeSubId == null) {
            return ResponseEntity.ok(new CheckoutStatusResponse("PENDING", SubscriptionPlan.FREE, null));
        }

        Subscription sub = subscriptionService.getOrCreateSubscription(user);

        return ResponseEntity.ok(new CheckoutStatusResponse("CONFIRMED", sub.getPlanCode(), stripeSubId));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> createCheckout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid CheckoutRequest request,
            HttpServletResponse response,
            HttpServletRequest req
    ) throws StripeException {
        User user = identityResolver.resolve(principal, req, response);
        Session session = billingService.createCheckoutSession(user, request.plan());

        CheckoutResponse res = new CheckoutResponse(session.getId(), session.getUrl(), session.getStatus());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/cancel")
    public ResponseEntity<CancellationResponse> cancelSubscription(@AuthenticationPrincipal UserPrincipal principal,
                                                                   HttpServletRequest req,
                                                                   HttpServletResponse response) throws StripeException {
        User user = identityResolver.resolve(principal, req, response);
        Subscription updated = billingService.cancelSubscriptionAtPeriodEnd(user);
        return ResponseEntity.ok(SubscriptionMapper.toCancelResponse(updated));
    }

    @PostMapping("/reactivate")
    public ResponseEntity<Void> reactivateSubscription(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest req,
                                                       HttpServletResponse response) throws StripeException {
        User user = identityResolver.resolve(principal, req, response);
        billingService.reactivateSubscription(user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/portal")
    public ResponseEntity<BillingPortalResponse> createPortalSession(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest req,
            HttpServletResponse response
    ) throws StripeException {
        User user = identityResolver.resolve(principal, req, response);

        com.stripe.model.billingportal.Session session =
                billingService.createBillingPortalSession(user);

        return ResponseEntity.ok(new BillingPortalResponse(session.getUrl()));
    }
}
