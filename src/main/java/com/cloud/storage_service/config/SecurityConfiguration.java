package com.cloud.storage_service.config;

import com.cloud.storage_service.properties.AppProperties;
import com.cloud.storage_service.properties.AuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.cloud.storage_service.constants.ApiConstant.AUTH0.JWKS_JSON;
import static com.cloud.storage_service.constants.GeneralConstant.DOUBLE_ASTERISKS;
import static com.cloud.storage_service.constants.GeneralConstant.SLASH;
import static com.cloud.storage_service.constants.SecurityConstant.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@SuppressWarnings({"PMD.SignatureDeclareThrowsException"})
public class SecurityConfiguration {
    public final AuthProperties authProperties;
    public final AppProperties appProperties;

    public static final String WILDCARD_PATH = SLASH + DOUBLE_ASTERISKS;

    @Bean
    public HeaderWriter xFrameOptionsHeaderWriter() {
        return new StaticHeadersWriter("X-Frame-Options", SAMEORIGIN);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", "Failed");
            errorResponse.put("code", HttpServletResponse.SC_UNAUTHORIZED);
            errorResponse.put("message", "Unauthorized Access");
            errorResponse.put("timestamp", Instant.now().toString());
            errorResponse.put("data", Collections.emptyList());

            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.addHeaderWriter(xFrameOptionsHeaderWriter()))
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .jwt(jwt -> jwt.jwkSetUri(authProperties.getIssuer() + JWKS_JSON))
                                .authenticationEntryPoint(unauthorizedEntryPoint())
                )
                .logout(logout -> logout.addLogoutHandler(logoutHandler()));

        configureAuthorization(http);
        return http.build();
    }

    public void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> {
            authorize
                    .requestMatchers(ADDITIONAL_PATHS).permitAll()
                    .requestMatchers(appProperties.getPublicApiPath() + DOUBLE_ASTERISKS).permitAll()
                    .requestMatchers(appProperties.getPrivateApiPath() + DOUBLE_ASTERISKS).authenticated()
                    .anyRequest().authenticated();
        });
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(ALLOWED_ORIGINS);
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(WILDCARD_PATH, configuration);
        return source;
    }

    private LogoutHandler logoutHandler() {
        return (request, response, authentication) -> {
            try {
                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                String finalUrl = authProperties.getIssuer() + "v2/logout?client_id="
                        + authProperties.getClientId() + "&returnTo=" + baseUrl;
                response.sendRedirect(finalUrl);
            } catch (IOException e) {
                throw new RuntimeException("Logout failed", e);
            }
        };
    }
}