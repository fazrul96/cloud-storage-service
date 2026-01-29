package com.cloud.storage_service.config;

import com.cloud.storage_service.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.cloud.storage_service.constants.GeneralConstant.DOUBLE_ASTERISKS;
import static com.cloud.storage_service.constants.GeneralConstant.SLASH;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {
    public static final String WILDCARD_PATH = SLASH + DOUBLE_ASTERISKS;
    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(corsProperties.getAllowedMethods());
        config.setAllowedHeaders(corsProperties.getAllowedHeaders());
        config.setAllowCredentials(corsProperties.isAllowCredentials());
        config.setExposedHeaders(List.of(
                "Content-Disposition"
        ));
        config.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(WILDCARD_PATH, config);
        return source;
    }
}