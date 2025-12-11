package com.cloud.storage_service.service;

import com.cloud.storage_service.dto.response.UploadListResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    UploadListResponseDto processUploadFiles(String requestId, List<MultipartFile> files, String prefix);
}