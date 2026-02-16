package com.lazydrop.modules.storage.service;

import com.lazydrop.modules.session.file.dto.SignedUploadResponse;

import java.io.IOException;

public interface StorageService {

    SignedUploadResponse createSignedUploadUrl(String folderPrefix, String fileName, String contentType, int expiresInSec) throws IOException;

    String createSignedDownloadUrl(String objectPath, int expiresInSec);

    void deleteFile(String objectPath);
}
