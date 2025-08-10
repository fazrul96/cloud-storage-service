package com.cloud.storage_service.config.aws;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "spring.cloud.aws")
public class S3Configuration {
    private String region;
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private String rootFolder;

    @Bean
    public S3Client s3Client() {
        try {
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKey, secretKey)
                            )
                    )
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create S3Client: " + e.getMessage(), e);
        }
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }
}