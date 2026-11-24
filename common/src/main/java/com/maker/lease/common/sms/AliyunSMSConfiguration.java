package com.maker.lease.common.sms;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliyunSMSProperties.class)
@ConditionalOnProperty(name = "aliyun.sms.endpoint")
public class AliyunSMSConfiguration {

    @Bean
    public AsyncClient smsClient(AliyunSMSProperties properties) {
        StaticCredentialProvider provider = StaticCredentialProvider.create(
                Credential.builder()
                        .accessKeyId(properties.getAccessKeyId())
                        .accessKeySecret(properties.getAccessKeySecret())
                        .build());

        try {
            return AsyncClient.builder()
                    .region("cn-hangzhou")
                    .credentialsProvider(provider)
                    .overrideConfiguration(
                            ClientOverrideConfiguration.create()
                                    .setEndpointOverride(properties.getEndpoint()))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("初始化阿里云短信客户端失败", e);
        }
    }
}
