package com.cloud.storage_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDto<T> {
    private String status;
    private String message;
    private String requestId;
    private String language;
    private String channel;
    private LocalDateTime timestamp;
    private int code;
    private T data;

    public ApiResponseDto(String status, String message, String requestId, String language, String channel, int code, T data) {
        this.status = status;
        this.message = message;
        this.requestId = requestId;
        this.language = language;
        this.channel = channel;
        this.code = code;
        this.data = data;
    }

    public ApiResponseDto(String requestId, String channel, String language) {
        requestId = requestId == null ? UUID.randomUUID().toString() : requestId;
        this.setRequestId(requestId);
        this.setChannel(channel);
        this.setLanguage(language);
        this.setTimestamp(LocalDateTime.now());
    }

    public void buildResponse(T response, HttpStatus httpStatus, String status, String responseDesc) {
        if (response != null) {
            this.setData(response);
        }

        this.buildResponse(httpStatus, status, responseDesc);
    }

    public void buildResponse(HttpStatus httpStatus, String status, String responseDesc) {
        this.setCode(httpStatus.value());
        this.setStatus(status);
        this.setMessage(responseDesc);
    }
}