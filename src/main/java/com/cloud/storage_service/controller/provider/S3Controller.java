package com.cloud.storage_service.controller.provider;

import com.cloud.storage_service.config.swagger.DefaultApiResponses;
import com.cloud.storage_service.constants.MessageConstants;
import com.cloud.storage_service.controller.BaseController;
import com.cloud.storage_service.dto.RequestContext;
import com.cloud.storage_service.dto.response.ApiResponseDto;
import com.cloud.storage_service.dto.response.UploadListResponseDto;
import com.cloud.storage_service.service.impl.S3ServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;
import java.util.Map;

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

    @Override
    protected String getControllerName() {
        return "S3Controller";
    }

    @Operation(
            summary = "Upload files to S3 storage",
            description = "Handles file upload to S3 bucket with optional prefix for key path."
    )
    @DefaultApiResponses
    @PostMapping(path = S3.UPLOAD_FILES)
    public ApiResponseDto<UploadListResponseDto> uploadFiles(
            RequestContext context,
            @RequestParam("files")
            @Parameter(name = "files", description = "List of files to upload.", required = true
            ) List<MultipartFile> files
    ) {
        logRequest(context.getRequestId(), "S3Controller.uploadFiles()");
        return handleRequest(context, () -> s3Service.processUploadFiles(
                context.getRequestId(), files, context.getPrefix()));
    }

    @Operation(summary = "Delete a file from S3")
    @DefaultApiResponses
    @DeleteMapping(path = S3.DELETE_FILE)
    public ApiResponseDto<String> deleteFile(RequestContext context) {
        logRequest(context.getRequestId(), "S3Controller.deleteFile()");

        HttpStatus httpStatus = HttpStatus.OK;

        s3Service.deleteFile(context.getFileName());
        return getResponseMessage(context.getLanguage(), context.getChannel(), context.getRequestId(), httpStatus,
                httpStatus.getReasonPhrase(), null, MessageConstants.HttpDescription.OK_DESC);
    }

    @Operation(summary = "Delete a folder from S3")
    @DefaultApiResponses
    @DeleteMapping(path = S3.DELETE_FOLDER)
    public ApiResponseDto<String> deleteFolder(RequestContext context) {
        logRequest(context.getRequestId(), "S3Controller.deleteFolder()");

        HttpStatus httpStatus = HttpStatus.OK;

        s3Service.deleteFolder(context.getFileName());
        return getResponseMessage(context.getLanguage(), context.getChannel(), context.getRequestId(), httpStatus,
                httpStatus.getReasonPhrase(), null, MessageConstants.HttpDescription.OK_DESC);
    }

    @Operation(summary = "Check if a file exists in S3")
    @DefaultApiResponses
    @GetMapping(path = S3.GET_FILE_EXISTS)
    public ApiResponseDto<Map<String, Boolean>> fileExists(RequestContext context) {
        logRequest(context.getRequestId(), "S3Controller.fileExists()");

        HttpStatus httpStatus = HttpStatus.OK;

        s3Service.fileExists(context.getFileName());
        return getResponseMessage(context.getLanguage(), context.getChannel(), context.getRequestId(), httpStatus,
                httpStatus.getReasonPhrase(), null, MessageConstants.HttpDescription.OK_DESC);
    }

    @Operation(summary = "Download a file from S3 using filename")
    @DefaultApiResponses
    @GetMapping(path = S3.DOWNLOAD_FILE)
    public void downloadFile(RequestContext context, HttpServletResponse response) {

        response.setStatus(HttpStatus.OK.value());

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Service.viewDownloadFile(context.getFileName())) {
            response.setContentType(s3Object.response().contentType());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                    + context.getFileName() + "\"");
            s3Object.transferTo(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("Download failed", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "Download a file from S3 using key")
    @DefaultApiResponses
    @GetMapping(path = S3.DOWNLOAD_FILE_BY_DOCUMENT_KEY)
    public void downloadFileByDocumentKey(@RequestParam("documentKey") String key, HttpServletResponse response) {
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Service.viewDownloadFile(key)) {
            String downloadName = key.substring(key.lastIndexOf('/') + 1);

            response.setContentType(s3Object.response().contentType());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"");
            s3Object.transferTo(response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            log.error("Download failed", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "Download a folder from S3")
    @DefaultApiResponses
    @GetMapping(path = S3.DOWNLOAD_FOLDER)
    public void downloadFolder(RequestContext context, HttpServletResponse response) {

        try {
            byte[] zipBytes = s3Service.downloadFolderAsZip(context.getFileName());

            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/zip");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                    + context.getFileName() + ".zip\"");
            response.setContentLength(zipBytes.length);

            response.getOutputStream().write(zipBytes);
            response.flushBuffer();

        } catch (Exception e) {
            log.error("Download failed", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "View a file from S3 inline")
    @DefaultApiResponses
    @GetMapping(path = S3.VIEW_FILE)
    public void viewFile(
            RequestContext context,
            HttpServletResponse response) {

        response.setStatus(HttpStatus.OK.value());
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Service.viewDownloadFile(context.getFileName())) {
            response.setContentType(s3Object.response().contentType());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\""
                    + context.getFileName() + "\"");
            s3Object.transferTo(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("View file failed", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "List files and folders from S3")
    @DefaultApiResponses
    @GetMapping(path = S3.LIST_FILES)
    public ApiResponseDto<Map<String, Object>> listFiles(RequestContext context) {
        logRequest(context.getRequestId(), "S3Controller.listFiles()");
        return handleRequest(context, () -> s3Service.listFiles(context.getPrefix()));
    }

    @Operation(summary = "Generate presigned URL to download a file from S3")
    @DefaultApiResponses
    @GetMapping(path = S3.PRESIGN_URL)
    public ApiResponseDto<String> getPresignedUrl(
            RequestContext context,
            @Parameter(description = "S3 key of the file to generate presigned URL for", required = true)
            @RequestParam String key
    ) {
        logRequest(context.getRequestId(), "S3Controller.getPresignedUrl()");
        return handleRequest(context, () -> s3Service.generatePresignedUrl(key));
    }
}
