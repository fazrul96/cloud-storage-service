package com.cloud.storage_service.controller;

import com.cloud.storage_service.dto.response.ApiResponseDto;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Abstract base controller to standardize API response formats across endpoints.
 * Not meant to be instantiated directly.
 */
@Slf4j
@NoArgsConstructor
public abstract class BaseController {
    protected String getResponseMessage(HttpStatus httpStatus) {
        return httpStatus.getReasonPhrase();
    }

    /**
     * Resolves and returns the request ID. If the provided requestId is null,
     * a new requestId will be generated.
     *
     * @param requestId The request ID passed in the request parameters.
     * @return The resolved or generated request ID.
     */
    protected String resolveRequestId(String requestId) {
        return (requestId == null || requestId.isBlank())
                ? UUID.randomUUID().toString()
                : requestId;
    }

    protected <T> ApiResponseDto<T> getResponseMessage(String language, String channel, String requestId, HttpStatus httpStatus, String status, T response, String message) {
        ApiResponseDto<T> apiResponse = new ApiResponseDto<>(requestId, channel, language);
        apiResponse.buildResponse(response, httpStatus, status, getResponseMessage(httpStatus));
        apiResponse.setMessage(message);
        return apiResponse;
    }

    protected <T> ApiResponseDto<T> getResponseMessage(String language, String channel, String requestId, HttpStatus httpStatus, String status, T response) {
        return getResponseMessage(language, channel, requestId, httpStatus, status, response, null);
    }

    /**
     * Logs the start of the request processing, including the method name and request ID.
     *
     * @param requestId The unique request ID for tracking.
     * @param methodName The name of the method that is being executed.
     */
    protected void logRequest(String requestId, String methodName) {
        log.info("[RequestId: {}] Execute {}", requestId, methodName);
    }

    protected void logRequest(String requestId, String methodName, Exception e) {
        log.error("[RequestId: {}] Execute {} ERROR: {}", requestId, methodName, e.getMessage());
    }
}