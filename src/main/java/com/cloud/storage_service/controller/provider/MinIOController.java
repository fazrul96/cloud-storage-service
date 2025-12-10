package com.cloud.storage_service.controller.provider;

import com.cloud.storage_service.config.swagger.DefaultApiResponses;
import com.cloud.storage_service.controller.BaseController;
import com.cloud.storage_service.service.impl.MinioServiceImpl;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static com.cloud.storage_service.constants.ApiConstant.MINIO;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "${app.privateApiPath}")
@CrossOrigin(origins = "${app.basePath}")
@Tag(name = "File Storage", description = "MinIo file provider management APIs")
public class MinIOController extends BaseController {
    private final MinioServiceImpl minioService;

    @Operation(summary = "Upload multiple files to MinIo")
    @DefaultApiResponses
    @PostMapping(path = MINIO.UPLOAD_FILES)
    public ResponseEntity<String> uploadFiles(
            @Parameter(description = "List of files to upload", required = true)
            @RequestParam("files") @NotEmpty List<MultipartFile> files,

            @Parameter(description = "Optional prefix for file keys", required = false)
            @RequestParam(value = "prefix", required = false) String prefix) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("No files uploaded");
            }
            List<String> uploadedFileNames = minioService.uploadFiles(files, null);
            return ResponseEntity.ok("Upload successful for files: " + uploadedFileNames);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            log.error("File upload failed", e); // Logging the error for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete a file from MinIo")
    @DefaultApiResponses
    @DeleteMapping(path = MINIO.DELETE_FILE)
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "Name of the file to delete", required = true)
            @RequestParam("fileName") String fileName) {

        try {
            minioService.deleteFile(fileName);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            log.error("File deletion failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File deletion failed: " + e.getMessage());
        }
    }

    @GetMapping(path = MINIO.GET_FILE_INFO)
    public ResponseEntity<String> getFileInfo(@RequestParam("fileName") String fileName) {
        try {
            String fileInfo = minioService.getFileInfo(fileName);
            return ResponseEntity.ok(fileInfo);
        } catch (Exception e) {
            log.error("Error retrieving file info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving file info: " + e.getMessage());
        }
    }

    @Operation(summary = "Download a file from MinIo")
    @DefaultApiResponses
    @PostMapping(path = MINIO.DOWNLOAD_FILE)
    public ResponseEntity<Void> downloadFile(@RequestParam("fileName") String fileName, HttpServletResponse response) {
        try {
            minioService.downloadFile(fileName, response);
            return ResponseEntity.ok().build(); // No content to return for the download
        } catch (Exception e) {
            log.error("Error downloading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "List files and folders from S3")
    @DefaultApiResponses
    @GetMapping(path = MINIO.LIST_FILES)
    public ResponseEntity<?> listFiles(
            @Parameter(description = "Optional prefix to filter files/folders")
            @RequestParam(required = false) String prefix) {
        try {
            List<String> fileNames = minioService.listFiles(prefix);
            List<Map<String, Object>> processedFiles = minioService.processFiles(fileNames, prefix);
            return ResponseEntity.ok(processedFiles);
        } catch (Exception e) {
            log.error("Error listing files from MinIO", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error listing files from MinIO", "throwable", e.getMessage()));
        }
    }
}