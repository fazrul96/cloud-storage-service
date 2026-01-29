package com.cloud.storage_service.component;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationErrorResolver {
    public String resolve(AuthenticationException exception) {
        Throwable cause = exception.getCause();

        if (cause instanceof JwtValidationException) {
            return "JWT token expired or invalid";
        }

        return "Unauthorized";
    }
}