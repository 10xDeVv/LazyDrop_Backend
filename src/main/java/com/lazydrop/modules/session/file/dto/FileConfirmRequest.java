package com.lazydrop.modules.session.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileConfirmRequest {
    @NotBlank(message = "objectPath is required")
    private String objectPath;

    @NotBlank(message = "originalName is required")
    private String originalName;

    @NotNull(message = "sizeBytes is required")
    private Long sizeBytes;
}
