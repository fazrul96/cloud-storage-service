package com.cloud.storage_service.component;

import com.cloud.storage_service.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import static com.cloud.storage_service.constants.GeneralConstant.DOUBLE_ASTERISKS;
import static com.cloud.storage_service.constants.SecurityConstant.ADDITIONAL_PATHS;

@Component
@RequiredArgsConstructor
public class AuthorizationPolicy {
    private final AppProperties appProperties;

    public void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers(ADDITIONAL_PATHS).permitAll()
                .requestMatchers(appProperties.getPublicApiPath() + DOUBLE_ASTERISKS).permitAll()
                .requestMatchers(appProperties.getPrivateApiPath() + DOUBLE_ASTERISKS).authenticated()
                .anyRequest().authenticated();
                //.anyRequest().permitAll();
    }
}