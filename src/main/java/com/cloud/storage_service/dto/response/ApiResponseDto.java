package com.cloud.storage_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDto<T> {
    private String status;
    private int code;
    private String message;
    private LocalDateTime timestamp;
    private T data;

    public ApiResponseDto(String status, int code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }
}
