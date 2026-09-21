package com.maker.lease.web.admin.service.impl;

import com.maker.lease.common.minio.MinioProperties;
import com.maker.lease.web.admin.service.FileService;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioProperties properties;

    @Override
    public String upload(MultipartFile file) {

        String contentType = file.getContentType();

        try {
            // bucket 不存在则创建
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucketName())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucketName())
                        .build());
                log.info("已创建 MinIO 存储桶：{}", properties.getBucketName());
            }
            // 设置为公共读，否则返回的 url 浏览器打不开
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(properties.getBucketName())
                    .config(publicReadPolicy(properties.getBucketName()))
                    .build());

            String originalFilename = file.getOriginalFilename();
            String objectName =new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/" +  UUID.randomUUID() + getSuffix(originalFilename);
            try (InputStream in = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(properties.getBucketName())
                                .object(objectName)
                                .stream(in, file.getSize(), -1)
                                .contentType(contentType)
                                .build());
            }
            String url = properties.getEndpoint() + "/" + properties.getBucketName() + "/" + objectName;
            log.info("上传成功，访问地址={}", url);
            return url;
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 取文件扩展名（含前导点），没有扩展名则返回空串
     */
    private String getSuffix(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int index = originalFilename.lastIndexOf('.');
        return index == -1 ? "" : originalFilename.substring(index);
    }

    private String publicReadPolicy(String bucketName) {
        return """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": [
                                "s3:GetBucketLocation",
                                "s3:ListBucket"
                            ],
                            "Resource": [
                                "arn:aws:s3:::%s"
                            ]
                        },
                        {
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": [
                                "s3:GetObject"
                            ],
                            "Resource": [
                                "arn:aws:s3:::%s/*"
                            ]
                        }
                    ]
                }
                """.formatted(bucketName, bucketName);
    }
}
