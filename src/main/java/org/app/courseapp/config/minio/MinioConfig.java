package org.app.courseapp.config.minio;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.public-url}")
    private String minioPublicUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    @Primary
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .region("us-east-1")
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean("publicMinioClient")
    public MinioClient publicMinioClient() {
        return MinioClient.builder()
                .endpoint(minioPublicUrl)
                .region("us-east-1")
                .credentials(accessKey, secretKey)
                .build();
    }
}