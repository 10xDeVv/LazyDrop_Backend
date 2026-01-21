package com.lazydrop.modules.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email
) {
}
