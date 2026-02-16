package com.lazydrop.modules.session.file.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SignedUploadResponse {
    private String signedUrl;
    private String objectPath;
    private String token;
    private int expiresIn;
}
