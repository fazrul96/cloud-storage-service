package com.cloud.storage_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponseDto {
    private String filename;
    private String path;
    private Long size;
    private String mimeType;
    private String url;
}