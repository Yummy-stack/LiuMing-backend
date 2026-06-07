package com.liumingservices.ai.rag.extract;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExtractMinIO {

    private final MinioClient minioClient;

    public List<Document> extractFromMinio(String bucket, String objectName) {
        log.info("开始从MinIO提取文件: {}/{}", bucket, objectName);
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build())) {
            
            TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(stream));
            List<Document> documents = reader.get();
            
            // 为每个文档添加元数据
            documents.forEach(doc -> {
                doc.getMetadata().put("source", "minio");
                doc.getMetadata().put("bucket", bucket);
                doc.getMetadata().put("object", objectName);
            });
            
            return documents;
        } catch (Exception e) {
            log.error("从MinIO提取文件失败: {}", objectName, e);
            return new ArrayList<>();
        }
    }
}

