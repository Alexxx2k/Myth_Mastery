package com.alexxx2k.springproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class YandexS3Config {

    @Value("${yandex.s3.endpoint}")
    private String endpoint;

    @Value("${yandex.s3.region}")
    private String region;

    @Value("${yandex.s3.access-key}")
    private String accessKey;

    @Value("${yandex.s3.secret-key}")
    private String secretKey;

    @Bean
    public S3Client yandexS3Client() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .serviceConfiguration(s3Config)
                .build();
    }
}
