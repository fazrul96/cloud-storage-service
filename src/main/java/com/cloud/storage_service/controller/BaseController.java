package com.cloud.storage_service.controller;

import com.cloud.storage_service.constants.MessageConstants;
import com.cloud.storage_service.dto.response.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;

/**
 * Abstract base controller to standardize API response formats across endpoints.
 * Not meant to be instantiated directly.
 */
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod"})
public abstract class BaseController {
    protected BaseController() {

    }

    /**
     * Returns a 200 OK response with a generic body.
     */
    protected <T> ResponseEntity<T> success(T body) {
        return ResponseEntity.ok(body);
    }

    /**
     * Returns a 201 Created response with a generic body.
     */
    protected <T> ResponseEntity<T> created(T body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * Returns a 204 No Content response.
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns a 500 Internal Server Error with a simple message body.
     */
    protected ResponseEntity<String> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    /**
     * Returns a standardized API success response (200 OK).
     */
    protected <T> ResponseEntity<ApiResponseDto<T>> apiSuccess(String message, T data) {
        return success(buildApiResponse(HttpStatus.OK, message, data));
    }

    /**
     * Returns a standardized API created response (201 Created).
     */
    protected <T> ResponseEntity<ApiResponseDto<T>> apiCreated(String message, T data) {
        return created(buildApiResponse(HttpStatus.CREATED, message, data));
    }

    /**
     * Returns a standardized API error response with the specified status code.
     */
    protected <T> ResponseEntity<ApiResponseDto<T>> apiError(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(
                new ApiResponseDto<>(
                        MessageConstants.ResponseMessages.FAILURE,
                        status.value(),
                        message,
                        null
                )
        );
    }

    private <T> ApiResponseDto<T> buildApiResponse(HttpStatus status, String message, T data) {
        return new ApiResponseDto<>(
                MessageConstants.ResponseMessages.SUCCESS,
                status.value(),
                message,
                data
        );
    }

    protected void validateRequiredParam(Object param, String fieldName) {
        boolean isInvalid = false;

        if (param == null) {
            isInvalid = true;
        } else if (param instanceof String && ((String) param).isBlank()) {
            isInvalid = true;
        } else if (param instanceof Collection && ((Collection<?>) param).isEmpty()) {
            isInvalid = true;
        }

        if (isInvalid) {
            throw new IllegalArgumentException(fieldName + " must not be null or empty");
        }
    }
}
