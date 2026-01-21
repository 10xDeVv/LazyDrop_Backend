package com.lazydrop.modules.subscription.model;

public record PlanLimits(
        SubscriptionPlan planCode,
        int maxActiveSessions,
        int sessionExpiryMinutes,
        int maxFilesPerSession,
        long maxFileSizeBytes,
        int maxParticipantsPerSession,
        int maxNotesPerSession,
        int maxNoteLength
) {}
