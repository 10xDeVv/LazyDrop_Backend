package com.lazydrop.modules.storage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazydrop.config.SupabaseProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import com.lazydrop.modules.session.file.dto.SignedUploadResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseStorageService implements StorageService {

    private final SupabaseProperties properties;
    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init(){
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }


    @Override
    public SignedUploadResponse createSignedUploadUrl(String folderPrefix, String fileName, String contentType, int expiresInSec) throws IOException {

        String objectPath = String.format("%s/%s/%s", folderPrefix, UUID.randomUUID().toString().substring(0, 8) ,fileName);


        String url = String.format("%s/storage/v1/object/upload/sign/%s/%s",
                properties.getUrl(),
                properties.getBucketName(),
                objectPath);

        String jsonBody = String.format("{\"expiresIn\": %d}", expiresInSec);


        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + properties.getServiceKey())
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try(Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                String error = response.body() != null ? response.body().string() : "Unknown";
                throw new IOException("Failed to create signed upload url: " + error);
            }

            String responseBody = response.body().string();
            JsonNode node = objectMapper.readTree(responseBody);
            String signedUrl = node.get("url").asText();
            String token = node.has("token") ? node.get("token").asText() : null;

            String fullSignedUrl = String.format("%s/storage/v1/%s", properties.getUrl(), signedUrl);

            log.info("Created signed upload url for: {} (expires in 5 min)", fileName);

            return SignedUploadResponse.builder()
                    .signedUrl(fullSignedUrl)
                    .objectPath(objectPath)
                    .token(token)
                    .expiresIn(expiresInSec)
                    .build();
        }
    }

    @Override
    public String createSignedDownloadUrl(String objectPath, int expiresInSeconds){
        String url = String.format("%s/storage/v1/object/sign/%s/%s",
                properties.getUrl(),
                properties.getBucketName(),
                objectPath);

        String jsonBody = String.format("{\"expiresIn\":%d}", expiresInSeconds);

        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + properties.getServiceKey())
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try(Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                String error = response.body() != null ? response.body().string() : "Unknown";
                throw new IOException("Failed to generate signed download URL: " + error);
            }

            String responseBody = response.body().string();
            JsonNode node = objectMapper.readTree(responseBody);
            String signedPath = node.get("signedURL").asText();

            String fileName = objectPath.substring(objectPath.lastIndexOf('/') + 1);
            String downloadParam = "&download=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);

            String fullSignedUrl = properties.getUrl() + "/storage/v1" + signedPath + downloadParam;

            log.info("Created signed download URL (expires in {} sec)", expiresInSeconds);
            return fullSignedUrl;
        }catch (IOException e) {
            log.error("Error creating signed download url for {}", objectPath, e);
            throw new RuntimeException("Failed to create signed download url", e);
        }
    }

    public void deleteFile(String objectPath){
        String url = String.format("%s/storage/v1/object/%s/%s",
                properties.getUrl(),
                properties.getBucketName(),
                objectPath);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + properties.getServiceKey())
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to delete file: " + objectPath);
            }
            log.info("Deleted: {}", objectPath);
        } catch (IOException e) {
            log.error("Error deleting file {}", objectPath, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
