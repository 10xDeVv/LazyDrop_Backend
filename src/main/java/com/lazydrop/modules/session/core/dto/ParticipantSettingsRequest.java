package com.lazydrop.modules.session.core.dto;

import jakarta.validation.constraints.NotNull;

public record ParticipantSettingsRequest(
        @NotNull Boolean autoDownload
) {
}
