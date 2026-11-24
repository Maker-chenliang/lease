package com.maker.lease.common.minio;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
/**
 * 条件注解 只有配置文件yml里存在 minio.endpoint 这个配置项时，这个配置类才生效；没有就整个跳过，连里面的 MinioClient Bean 都不创建，防止
 * 没配置 minio.endpoint 启动不起来。
 */
@ConditionalOnProperty(name = "minio.endpoint")
public class MinioConfiguration {
    @Bean
    public MinioClient minioClient(MinioProperties properties) throws Exception {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .region(properties.getRegion())
                .build();

        return minioClient;
    }
}
