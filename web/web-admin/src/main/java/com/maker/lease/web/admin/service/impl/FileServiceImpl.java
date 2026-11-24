package com.maker.lease.web.admin.service.impl;

import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.minio.MinioProperties;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.web.admin.service.FileService;
import io.minio.*;
import io.minio.errors.*;
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
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    /**
     * 允许上传的图片扩展名
     */
    private static final Pattern IMAGE_SUFFIX =
            Pattern.compile("^\\.(jpe?g|png|gif|bmp|webp)$", Pattern.CASE_INSENSITIVE);

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioProperties properties;

    @Override
    public String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        if (file == null || file.isEmpty()) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        // 扩展名是客户端给的，先做一层白名单过滤
        String suffix = getSuffix(file.getOriginalFilename());
        if (!IMAGE_SUFFIX.matcher(suffix).matches()) {
            log.warn("拒绝上传：扩展名不合法，filename={}", file.getOriginalFilename());
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        // 以文件头为准判断真实内容，同时得到服务端认定的 Content-Type
        String contentType = detectImageContentType(file);
        if (contentType == null) {
            log.warn("拒绝上传：文件内容不是图片，filename={}", file.getOriginalFilename());
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

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

            // 存储用的扩展名以检测出的真实类型为准，不用客户端文件名
            String objectName = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/"
                    + UUID.randomUUID() + getExtensionByContentType(contentType);
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

    /**
     * 读文件头（magic number）判断真实图片类型，返回对应的 Content-Type；
     * 不是图片返回 null。SVG 没有文件头且可能携带脚本，不支持。
     */
    private String detectImageContentType(MultipartFile file) throws IOException {
        byte[] h = new byte[12];
        int n;
        try (InputStream in = file.getInputStream()) {
            n = in.readNBytes(h, 0, h.length);
        }

        // JPEG: FF D8 FF
        if (n >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (n >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                && (h[4] & 0xFF) == 0x0D && (h[5] & 0xFF) == 0x0A
                && (h[6] & 0xFF) == 0x1A && (h[7] & 0xFF) == 0x0A) {
            return "image/png";
        }
        // GIF: GIF87a / GIF89a
        if (n >= 6 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8'
                && (h[4] == '7' || h[4] == '9') && h[5] == 'a') {
            return "image/gif";
        }
        // BMP: BM
        if (n >= 2 && h[0] == 'B' && h[1] == 'M') {
            return "image/bmp";
        }
        // WEBP: RIFF + 4 字节长度 + WEBP
        if (n >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    /**
     * 存储用的扩展名，来自服务端检测出的真实类型
     */
    private String getExtensionByContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/bmp" -> ".bmp";
            case "image/webp" -> ".webp";
            default -> "";
        };
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
