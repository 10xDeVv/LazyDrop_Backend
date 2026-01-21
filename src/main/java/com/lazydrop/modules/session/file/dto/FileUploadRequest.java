package com.lazydrop.modules.session.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileUploadRequest {

    @NotBlank(message = "Missing fileName")
    private String fileName;

    @NotBlank(message = "Missing content-type")
    private String contentType;

    @NotNull(message = "File size cannot be null")
    private long fileSize;
}
