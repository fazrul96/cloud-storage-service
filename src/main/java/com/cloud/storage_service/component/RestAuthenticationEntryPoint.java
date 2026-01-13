package com.cloud.storage_service.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloud.storage_service.dto.response.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        ApiResponseDto<Void> body = ApiResponseDto.unauthorized(resolveMessage(exception));

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private String resolveMessage(AuthenticationException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof JwtValidationException) {
            return "JWT token expired or invalid";
        }
        return "Unauthorized";
    }
}