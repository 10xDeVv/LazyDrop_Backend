package com.lazydrop.modules.billing.service;

import com.lazydrop.common.exception.PlanLimitExceededException;
import com.lazydrop.modules.session.core.model.DropSession;
import com.lazydrop.modules.subscription.model.PlanLimits;
import com.lazydrop.modules.subscription.service.SubscriptionService;
import com.lazydrop.modules.user.model.User;
import com.lazydrop.modules.session.core.repository.DropSessionRepository;
import com.lazydrop.modules.session.file.repository.DropFileRepository;
import com.lazydrop.modules.session.core.dto.DropSessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanEnforcementService {

    private final SubscriptionService subscriptionService;
    private final DropSessionRepository sessionRepo;
    private final DropFileRepository fileRepo;

    public void checkSessionCreationLimit(User owner) {
        PlanLimits limits = subscriptionService.getLimitsForUser(owner);
        long activeCount = sessionRepo.countByOwnerAndStatusInAndExpiresAtAfter(
                owner,
                List.of(DropSessionStatus.OPEN, DropSessionStatus.CONNECTED),
                Instant.now()
        );


        if (activeCount >= limits.maxActiveSessions()) {
            throw new PlanLimitExceededException("You've reached the maximum active sessions for your " + limits.planCode() + " plan.");
        }
    }

    public void checkFileUploadLimits(DropSession session, Long fileSizeBytes) {
        PlanLimits limits = subscriptionService.getLimitsForUser(session.getOwner());
        long currentFileCount = fileRepo.countByDropSession(session);

        if (fileSizeBytes > limits.maxFileSizeBytes()) {
            throw new PlanLimitExceededException("File size exceeds the " + (limits.maxFileSizeBytes() / 1024 / 1024) + "MB limit for the owner's plan.");
        }

        if (currentFileCount >= limits.maxFilesPerSession()) {
            throw new PlanLimitExceededException("This session has reached the maximum file count allowed by the owner's plan.");
        }
    }

    public void checkParticipantLimit(User sessionOwner, long currentParticipantCount) {
        PlanLimits limits = subscriptionService.getLimitsForUser(sessionOwner);
        if (currentParticipantCount >= limits.maxParticipantsPerSession()) {
            throw new PlanLimitExceededException("This session has reached the maximum participant limit.");
        }
    }
}