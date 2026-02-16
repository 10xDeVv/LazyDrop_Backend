package com.lazydrop.modules.session.file.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record DropFileDto(
        String id,
        String originalName,
        long sizeBytes,
        Instant createdAt,
        boolean downloadedByMe
) {}
