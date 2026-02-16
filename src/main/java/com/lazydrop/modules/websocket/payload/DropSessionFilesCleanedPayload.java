package com.lazydrop.modules.websocket.payload;

public record DropSessionFilesCleanedPayload(
        String id,
        int fileCount,
        long totalBytes
) {
}
