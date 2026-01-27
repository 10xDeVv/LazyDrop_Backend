package com.lazydrop.modules.session.participant.dto;

import jakarta.validation.constraints.NotNull;

public record ParticipantSettingsRequest(
        @NotNull Boolean autoDownload
) {
}
