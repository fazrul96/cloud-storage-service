package com.cloud.storage_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.cloud.storage_service.constants.GeneralConstant.DOUBLE_ASTERISKS;
import static com.cloud.storage_service.constants.GeneralConstant.SLASH;
import static com.cloud.storage_service.constants.SecurityConstant.*;

@Configuration
public class CorsConfig {
    public static final String WILDCARD_PATH = SLASH + DOUBLE_ASTERISKS;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(ALLOWED_HEADERS);
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of(
                "Content-Disposition"
        ));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(WILDCARD_PATH, config);
        return source;
    }
}