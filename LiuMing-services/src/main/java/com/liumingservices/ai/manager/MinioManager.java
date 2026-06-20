package com.liumingservices.ai.manager;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class MinioManager {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String BUCKET_NAME;

    @Value("${minio.endpoint}")
    private String ENDPOINT;

    /**
     * 上传文件到 MinIO
     *
     * @param multipartFile 上传的文件
     * @return 文件的完整访问 URL
     */
    public String uploadFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new RuntimeException("上传的文件为空");
        }

        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String objectName = UUID.randomUUID().toString().replace("-", "") + extension;
            InputStream inputStream = multipartFile.getInputStream();
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .stream(inputStream, multipartFile.getSize(), -1)
                    .contentType(multipartFile.getContentType())
                    .build();

            minioClient.putObject(putObjectArgs);

            String formattedEndpoint = ENDPOINT.endsWith("/") ? ENDPOINT : ENDPOINT + "/";
            String fileUrl = formattedEndpoint + BUCKET_NAME + "/" + objectName;

            return fileUrl;

        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据完整的文件 URL 地址删除 MinIO 中的文件
     *
     * @param fileUrl 文件的完整 URL (例如: <a href="http://127.0.0.1:9000/bucketName/uuid.jpg">...</a>)
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("文件URL不能为空");
        }

        try {
            String objectName = extractObjectNameFromUrl(fileUrl);
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .build();

            minioClient.removeObject(removeObjectArgs);

        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 URL 或相对路径中解析 MinIO ObjectName
     */
    public String resolveObjectName(String filePathOrUrl) {
        if (filePathOrUrl == null || filePathOrUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        if (filePathOrUrl.startsWith("http")) {
            return extractObjectNameFromUrl(filePathOrUrl);
        }
        return filePathOrUrl.startsWith("/") ? filePathOrUrl.substring(1) : filePathOrUrl;
    }

    /**
     * 从完整的 URL 中提取 ObjectName (包含路径前缀的相对文件名)
     */
    private String extractObjectNameFromUrl(String fileUrl) {
        String prefix = ENDPOINT.endsWith("/") ? ENDPOINT + BUCKET_NAME + "/" : ENDPOINT + "/" + BUCKET_NAME + "/";

        // 1. 如果 URL 是以当前 endpoint + bucket 开头的，直接截取后面的部分
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }

        // 2. 备用策略：直接通过 bucket 名称进行截取 (适用于内网外网 endpoint 不一致的情况)
        int bucketIndex = fileUrl.indexOf("/" + BUCKET_NAME + "/");
        if (bucketIndex != -1) {
            return fileUrl.substring(bucketIndex + BUCKET_NAME.length() + 2);
        }

        throw new RuntimeException("无法解析文件路径中的 ObjectName: " + fileUrl);
    }

}
