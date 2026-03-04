package com.lazydrop.modules.storage.service;

import com.lazydrop.modules.session.file.dto.SignedUploadResponse;

import java.io.InputStream;

public interface StorageService {

    SignedUploadResponse createSignedUploadUrl(String folderPrefix, String fileName, String contentType, int expiresInSec);

    /** Upload file bytes directly to object storage (server-side). Returns the object path. */
    String uploadFile(String folderPrefix, String fileName, String contentType, long contentLength, InputStream data);

    String createSignedDownloadUrl(String objectPath, int expiresInSec);

    void deleteFile(String objectPath);
}
