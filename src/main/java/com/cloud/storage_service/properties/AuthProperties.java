package com.cloud.storage_service.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@Data
@ConfigurationProperties("okta.oauth2")
public class AuthProperties {
    private String issuer;
    private String clientId;
    private String clientSecret;
    private String audience;
    private String grantType;
}
