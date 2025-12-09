package com.cloud.storage_service.controller.provider;

import com.cloud.storage_service.constants.GeneralConstant;
import com.cloud.storage_service.constants.MessageConstants;
import com.cloud.storage_service.controller.BaseController;
import com.cloud.storage_service.dto.response.ApiResponseDto;
import com.cloud.storage_service.dto.response.UploadListResponseDto;
import com.cloud.storage_service.dto.response.UploadResponseDto;
import com.cloud.storage_service.exception.WebException;
import com.cloud.storage_service.service.impl.S3ServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.cloud.storage_service.constants.ApiConstant.S3;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "${app.privateApiPath}")
@CrossOrigin(origins = "${app.basePath}")
@Tag(name = "File Storage", description = "S3 file provider management APIs")
public class S3Controller extends BaseController {
    private final S3ServiceImpl s3Service;

    @Operation(
            summary = "Upload files to S3 storage",
            description = "Handles file upload to S3 bucket with optional prefix for key path."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageConstants.HttpCodes.OK,
                    description = MessageConstants.HttpDescription.OK_DESC),
            @ApiResponse(responseCode = MessageConstants.HttpCodes.BAD_REQUEST,
                    description = MessageConstants.HttpDescription.BAD_REQUEST_DESC),
            @ApiResponse(responseCode = MessageConstants.HttpCodes.INTERNAL_SERVER_ERROR,
                    description = MessageConstants.HttpDescription.INTERNAL_ERROR_DESC)
    })
    @PostMapping(path = S3.UPLOAD_FILES)
    public ApiResponseDto<UploadListResponseDto> uploadFiles(
            @RequestHeader("userId") String userId,
            @RequestParam("files")
            @Parameter(name = "files", description = "List of files to upload.", required = true
            ) List<MultipartFile> files,
            @RequestParam(value = "prefix", required = false)
            @Parameter(name = "prefix", description = "Optional prefix added to the file key paths.",
                    example = "documents/"
            ) String prefix,
            @RequestParam(value = "language", required = false, defaultValue = GeneralConstant.Language.IN_ID)
            @Parameter(name = "language", description = "Locale for response localization (e.g., en_US, in_ID)."
            ) String language,
            @RequestParam(value = "channel", required = false, defaultValue = "web")
            @Parameter(
                    name = "channel", description = "Source of request such as web or mobile.", example = "web"
            ) String channel,
            @RequestParam(value = "requestId", required = false)
            @Parameter(
                    name = "requestId",
                    description = "Unique identifier for the request. Auto-generated if missing.",
                    example = "c1f23ba4-9123-4bd8-a1ea-9a123456abcd"
            ) String requestId
    ) {
        requestId = resolveRequestId(requestId);
        log.info("[RequestId: {}] Starting S3Controller.uploadFiles()", requestId);

        HttpStatus httpStatus = HttpStatus.OK;

        try {
            UploadListResponseDto response = s3Service.processUploadFiles(requestId, files, prefix);
            return getResponseMessage(language, channel, requestId, httpStatus, httpStatus.getReasonPhrase(), response, MessageConstants.HttpDescription.OK_DESC);

        } catch (WebException we) {
            log.error("[RequestId: {}] S3Controller.uploadFiles() ERROR {}", requestId, we.getMessage());
            return getResponseMessage(language, channel, requestId, HttpStatus.BAD_REQUEST, MessageConstants.HttpDescription.BAD_REQUEST_DESC, null, we.getMessage());

        } catch (Exception e) {
            log.error("[RequestId: {}] S3Controller.uploadFiles() ERROR {}", requestId, e.getMessage());
            return getResponseMessage(language, channel, requestId, HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.HttpDescription.INTERNAL_ERROR_DESC, null, e.getMessage());
        }
    }


//    @Operation(summary = "Delete a file from S3")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = OK, description = "File deleted successfully"),
//            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Deletion failed")
//    })
//    @DeleteMapping(path = S3.DELETE_FILE)
//    public ResponseEntity<ApiResponseDto<String>> deleteFile(
//            @Parameter(description = "Name of the file to delete", required = true)
//            @RequestParam("fileName") String fileName) {
//
//        s3Service.deleteFile(fileName);
//        return apiSuccess("File deleted successfully", fileName);
//    }
//
//    @Operation(summary = "Delete a folder from S3")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = OK, description = "File deleted successfully"),
//            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Deletion failed")
//    })
//    @DeleteMapping(path = S3.DELETE_FOLDER)
//    public ResponseEntity<ApiResponseDto<String>> deleteFolder(
//            @Parameter(description = "Name of the folder to delete", required = true)
//            @RequestParam("folderName") String folderName) {
//
//        s3Service.deleteFolder(folderName);
//        return apiSuccess("Folder deleted successfully", folderName);
//    }
//
//    @Operation(summary = "Check if a file exists in S3")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = OK, description = "Check complete"),
//            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Check failed")
//    })
//    @GetMapping(path = S3.GET_FILE_EXISTS)
//    public ResponseEntity<ApiResponseDto<Map<String, Boolean>>> fileExists(
//            @Parameter(description = "Name of the file to check", required = true)
//            @RequestParam("fileName") String fileName) {
//        boolean exists = s3Service.fileExists(fileName);
//        return apiSuccess("Check complete", Map.of("exists", exists));
//    }
//
//    @Operation(summary = "Download a file from S3")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = OK, description = OK_DESC),
//            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESC),
//            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = INTERNAL_ERROR_DESC)
//    })
//    @GetMapping(path = S3.DOWNLOAD_FILE)
//    public void downloadFile(
//            @Parameter(description = "Name of the file to download", required = true)
//            @RequestParam("fileName") String fileName,
//            HttpServletResponse response) {
//
//        response.setStatus(HttpStatus.OK.value());
//        try (ResponseInputStream<GetObjectResponse> s3Object = s3Service.viewDownloadFile(fileName)) {
//            response.setContentType(s3Object.response().contentType());
//            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
//            s3Object.transferTo(response.getOutputStream());
//            response.flushBuffer();
//        } catch (Exception e) {
//            log.error("Download failed", e);
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
//    }
//
//    @Operation(summary = "Download a folder from S3")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = OK, description = OK_DESC),
//            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESC),
//            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = INTERNAL_ERROR_DESC)
//    })
//    @GetMapping(path = S3.DOWNLOAD_FOLDER)
//    public void downloadFolder(
//            @Parameter(description = "Name of the folder to download", required = true)
//            @RequestParam("folderName") String folderName,
//            HttpServletResponse response) {
//
//        try {
//            byte[] zipBytes = s3Service.downloadFolderAsZip(folderName);
//
//            response.setStatus(HttpStatus.OK.value());
//            response.setContentType("application/zip");
//            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + folderName + ".zip\"");
//            response.setContentLength(zipBytes.length);
//
//            response.getOutputStream().write(zipBytes);
//            response.flushBuffer();
//
//        } catch (Exception e) {
//            log.error("Download failed", e);
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
//    }
//
//    @Operation(summary = "View a file from S3 inline")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = OK, description = OK_DESC),
//            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESC),
//            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = INTERNAL_ERROR_DESC)
//    })
//    @GetMapping(path = S3.VIEW_FILE)
//    public void viewFile(
//            @Parameter(description = "Name of the file to view", required = true)
//            @RequestParam("fileName") String fileName,
//            HttpServletResponse response) {
//
//        response.setStatus(HttpStatus.OK.value());
//        try (ResponseInputStream<GetObjectResponse> s3Object = s3Service.viewDownloadFile(fileName)) {
//            response.setContentType(s3Object.response().contentType());
//            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
//            s3Object.transferTo(response.getOutputStream());
//            response.flushBuffer();
//        } catch (Exception e) {
//            log.error("View file failed", e);
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
//    }
//
//    @Operation(summary = "List files and folders from S3")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = OK, description = "Files retrieved successfully"),
//            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "List operation failed")
//    })
//    @GetMapping(path = S3.LIST_FILES)
//    public ResponseEntity<ApiResponseDto<Map<String, Object>>> listFiles(
//            @Parameter(description = "Optional prefix to filter files/folders")
//            @RequestParam(required = false) String prefix) {
//        Map<String, Object> result = s3Service.listFiles(prefix);
//        return apiSuccess("Files retrieved successfully", result);
//    }
//
//    @Operation(summary = "Generate presigned URL to download a file from S3")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = OK, description = "Presigned URL generated successfully"),
//            @ApiResponse(responseCode = BAD_REQUEST, description = "Invalid request"),
//            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Failed to generate presigned URL")
//    })
//    @GetMapping(path = S3.PRESIGN_URL)
//    public ResponseEntity<ApiResponseDto<String>> getPresignedUrl(
//            @Parameter(description = "S3 key of the file to generate presigned URL for", required = true)
//            @RequestParam String key) {
//        try {
//            validateRequiredParam(key, "File key");
//            String presignedUrl = s3Service.generatePresignedUrl(key);
//            return apiSuccess("Presigned URL generated successfully", presignedUrl);
//        } catch (Exception e) {
//            log.error("Failed to generate presigned URL for key: {}", key, e);
//            return apiError(ERROR,  HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
}
