package com.cloud.storage_service.controller.storage;

import com.cloud.storage_service.constants.GeneralConstant;
import com.cloud.storage_service.controller.BaseController;
import com.cloud.storage_service.dto.response.ApiResponseDto;
import com.cloud.storage_service.service.storage.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cloud.storage_service.constants.ApiConstant.S3;
import static com.cloud.storage_service.constants.MessageConstants.HttpCodes.*;
import static com.cloud.storage_service.constants.MessageConstants.HttpDescription.*;
import static com.cloud.storage_service.constants.MessageConstants.ResponseMessages.ERROR;
import static com.cloud.storage_service.util.common.StringUtils.normalizePrefix;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "${app.publicApiPath}")
@CrossOrigin(origins = "${app.basePath}")
@Tag(name = "File Storage", description = "S3 file storage management APIs")
@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
public class S3Controller extends BaseController {
    private final S3Service s3Service;

    @Operation(summary = "Upload multiple files to S3")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "Upload successful"),
            @ApiResponse(responseCode = BAD_REQUEST, description = "No files uploaded"),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Upload failed")
    })
    @PostMapping(path = S3.UPLOAD_FILES)
    public ResponseEntity<ApiResponseDto<List<String>>> uploadFiles(
            @Parameter(description = "List of files to upload", required = true)
            @RequestParam("files") @NotEmpty List<MultipartFile> files,

            @Parameter(description = "Optional prefix for file keys", required = false)
            @RequestParam(value = "prefix", required = false) String prefix) {

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        List<String> uploadedKeys = files.stream().map(file -> {
            String key = normalizePrefix(prefix)
                    + UUID.randomUUID()
                    + GeneralConstant.DASH
                    + file.getOriginalFilename();
            try {
                s3Service.uploadFile(
                        key,
                        file.getInputStream(),
                        file.getSize(),
                        file.getContentType()
                );
                return key;
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to upload file: " + file.getOriginalFilename(), e);
            }
        }).collect(Collectors.toList());

        return apiSuccess("Upload successful", uploadedKeys);
    }

    @Operation(summary = "Delete a file from S3")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "File deleted successfully"),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Deletion failed")
    })
    @DeleteMapping(path = S3.DELETE_FILE)
    public ResponseEntity<ApiResponseDto<String>> deleteFile(
            @Parameter(description = "Name of the file to delete", required = true)
            @RequestParam("fileName") String fileName) {

        s3Service.deleteFile(fileName);
        return apiSuccess("File deleted successfully", fileName);
    }

    @Operation(summary = "Delete a folder from S3")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "File deleted successfully"),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Deletion failed")
    })
    @DeleteMapping(path = S3.DELETE_FOLDER)
    public ResponseEntity<ApiResponseDto<String>> deleteFolder(
            @Parameter(description = "Name of the folder to delete", required = true)
            @RequestParam("folderName") String folderName) {

        s3Service.deleteFolder(folderName);
        return apiSuccess("Folder deleted successfully", folderName);
    }

    @Operation(summary = "Check if a file exists in S3")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "Check complete"),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Check failed")
    })
    @GetMapping(path = S3.GET_FILE_EXISTS)
    public ResponseEntity<ApiResponseDto<Map<String, Boolean>>> fileExists(
            @Parameter(description = "Name of the file to check", required = true)
            @RequestParam("fileName") String fileName) {
        boolean exists = s3Service.fileExists(fileName);
        return apiSuccess("Check complete", Map.of("exists", exists));
    }

    @Operation(summary = "Download a file from S3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = OK_DESC),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESC),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = INTERNAL_ERROR_DESC)
    })
    @GetMapping(path = S3.DOWNLOAD_FILE)
    public void downloadFile(
            @Parameter(description = "Name of the file to download", required = true)
            @RequestParam("fileName") String fileName,
            HttpServletResponse response) {

        response.setStatus(HttpStatus.OK.value());
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Service.viewDownloadFile(fileName)) {
            response.setContentType(s3Object.response().contentType());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            s3Object.transferTo(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("Download failed", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "Download a folder from S3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = OK_DESC),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESC),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = INTERNAL_ERROR_DESC)
    })
    @GetMapping(path = S3.DOWNLOAD_FOLDER)
    public void downloadFolder(
            @Parameter(description = "Name of the folder to download", required = true)
            @RequestParam("folderName") String folderName,
            HttpServletResponse response) {

        try {
            byte[] zipBytes = s3Service.downloadFolderAsZip(folderName);

            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/zip");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + folderName + ".zip\"");
            response.setContentLength(zipBytes.length);

            response.getOutputStream().write(zipBytes);
            response.flushBuffer();

        } catch (Exception e) {
            log.error("Download failed", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "View a file from S3 inline")
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = OK_DESC),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESC),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = INTERNAL_ERROR_DESC)
    })
    @GetMapping(path = S3.VIEW_FILE)
    public void viewFile(
            @Parameter(description = "Name of the file to view", required = true)
            @RequestParam("fileName") String fileName,
            HttpServletResponse response) {

        response.setStatus(HttpStatus.OK.value());
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Service.viewDownloadFile(fileName)) {
            response.setContentType(s3Object.response().contentType());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
            s3Object.transferTo(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("View file failed", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "List files and folders from S3")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "Files retrieved successfully"),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "List operation failed")
    })
    @GetMapping(path = S3.LIST_FILES)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> listFiles(
            @Parameter(description = "Optional prefix to filter files/folders")
            @RequestParam(required = false) String prefix) {
        Map<String, Object> result = s3Service.listFiles(prefix);
        return apiSuccess("Files retrieved successfully", result);
    }

    @Operation(summary = "Generate presigned URL to download a file from S3")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "Presigned URL generated successfully"),
            @ApiResponse(responseCode = BAD_REQUEST, description = "Invalid request"),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Failed to generate presigned URL")
    })
    @GetMapping(path = S3.PRESIGN_URL)
    public ResponseEntity<ApiResponseDto<String>> getPresignedUrl(
            @Parameter(description = "S3 key of the file to generate presigned URL for", required = true)
            @RequestParam String key) {
        try {
            validateRequiredParam(key, "File key");
            String presignedUrl = s3Service.generatePresignedUrl(key);
            return apiSuccess("Presigned URL generated successfully", presignedUrl);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", key, e);
            return apiError(ERROR,  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
