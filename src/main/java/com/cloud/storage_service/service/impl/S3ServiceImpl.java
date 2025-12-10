package com.cloud.storage_service.service.impl;

import com.cloud.storage_service.config.aws.S3Configuration;
import com.cloud.storage_service.constants.GeneralConstant;
import com.cloud.storage_service.dto.response.UploadListResponseDto;
import com.cloud.storage_service.dto.response.UploadResponseDto;
import com.cloud.storage_service.exception.WebException;
import com.cloud.storage_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.cloud.storage_service.util.common.StringUtils.normalizePrefix;
import static software.amazon.awssdk.http.HttpStatusCode.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;
    private final S3Configuration s3Configuration;
    private final S3Presigner s3Presigner;

    /**
     * Upload a file to S3 with the specified key.
     */
    public UploadListResponseDto processUploadFiles(String requestId, List<MultipartFile> files, String prefix) throws WebException {
        log.info("[RequestId: {}] Starting S3ServiceImpl.processUploadFiles()", requestId);

        validateFiles(files);

        String normalizedPrefix = normalizePrefix(prefix);
        List<UploadResponseDto> response = files.stream()
                .map(file -> uploadSingleFile(requestId, file, normalizedPrefix))
                .map(this::toUploadResponse)
                .collect(Collectors.toList());

        return new UploadListResponseDto(response);
    }

    private UploadResponseDto uploadSingleFile(String requestId, MultipartFile file, String prefix) {
        log.info("[RequestId: {}] Starting S3ServiceImpl.uploadSingleFile()", requestId);

        String normalized = normalizePrefix(prefix);

        String key = normalized.isEmpty()
                ? file.getOriginalFilename()
                : normalized + "/" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Configuration.getBucketName())
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
        }

        return buildUploadResponse(key, file.getContentType(), file.getSize());
    }

    private UploadResponseDto toUploadResponse(UploadResponseDto response) {
        return UploadResponseDto.builder()
                .filename(response.getFilename())
                .path(response.getPath())
                .mimeType(response.getMimeType())
                .url(response.getUrl())
                .size(response.getSize())
                .build();
    }

    private UploadResponseDto buildUploadResponse(String key, String contentType, long size) {
        String bucket = s3Configuration.getBucketName();
        String region = s3Configuration.getRegion();

        return UploadResponseDto.builder()
                .filename(extractFilename(key))
                .path(key)
                .mimeType(contentType)
                .url(String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key))
                .size(size)
                .build();
    }

    private String extractFilename(String key) {
        int index = key.lastIndexOf('/');
        return (index != -1) ? key.substring(index + 1) : key;
    }

    /**
     * Download a file from S3 by key.
     */
    public ResponseInputStream<GetObjectResponse> viewDownloadFile(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Configuration.getBucketName())
                .key(key)
                .build();
        return s3Client.getObject(request);
    }

    public byte[] downloadFolderAsZip(String folderKey) throws IOException {
        if (!folderKey.endsWith("/")) {
            folderKey += "/";
        }

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(s3Configuration.getBucketName())
                .prefix(folderKey)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(zipOutputStream);

        for (S3Object s3Object : listResponse.contents()) {
            String key = s3Object.key();

            if (key.endsWith("/")) {
                continue;
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Configuration.getBucketName())
                    .key(key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(getObjectRequest)) {
                String zipEntryName = key.substring(folderKey.length());

                zipOut.putNextEntry(new ZipEntry(zipEntryName));
                s3InputStream.transferTo(zipOut);
                zipOut.closeEntry();
            }
        }

        zipOut.close();
        return zipOutputStream.toByteArray(); // You can return this or write it to HTTP response
    }

    public void deleteFolder(String folderKey) {
        if (!folderKey.endsWith("/")) {
            folderKey += "/";
        }

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(s3Configuration.getBucketName())
                .prefix(folderKey)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

        List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                .collect(Collectors.toList());

        if (!objectsToDelete.isEmpty()) {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(s3Configuration.getBucketName())
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();

            s3Client.deleteObjects(deleteRequest);
        }
    }

    /**
     * Delete a file from S3 by key.
     */
    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(s3Configuration.getBucketName())
                .key(key)
                .build());
    }

    /**
     * Check if a file exists in S3.
     */
    public boolean fileExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Configuration.getBucketName())
                    .key(key)
                    .build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    /**
     * List files and folders under the specified prefix.
     * If prefix is null, uses a default base prefix.
     */
    public Map<String, Object> listFiles(String prefix) {
        String basePrefix = prefix != null ? prefix : "webtoons-content/";

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(s3Configuration.getBucketName())
                .prefix(basePrefix)
                .delimiter(GeneralConstant.SLASH)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<String> folders = response.commonPrefixes().stream()
                .map(CommonPrefix::prefix)
                .collect(Collectors.toList());

        List<Map<String, Object>> files = response.contents().stream()
                .filter(obj -> !obj.key().equals(basePrefix))
                .sorted(Comparator.comparing(S3Object::lastModified).reversed())
                .map(obj -> {
                    Map<String, Object> fileMap = new HashMap<>();
                    fileMap.put("name", obj.key());
                    fileMap.put("lastModified", obj.lastModified().toString());
                    fileMap.put("size", obj.size());
                    return fileMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("folders", folders);
        result.put("files", files);

        return result;
    }

    public String generatePresignedUrl(String keyName) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(s3Configuration.getBucketName())
                .key(keyName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toExternalForm();
    }

    private void validateFiles(List<MultipartFile> files) throws WebException {
        if (files == null || files.isEmpty()) {
            throw new WebException("Uploaded file list cannot be empty");
        }
    }
}