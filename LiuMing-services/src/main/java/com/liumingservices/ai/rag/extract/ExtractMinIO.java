package com.liumingservices.ai.rag.extract;

import cn.hutool.core.lang.hash.Hash;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            HashMap<String, Object> metadataMap = new HashMap<>();

            metadataMap.put("relatedEntityId", "23");
            metadataMap.put("bucket", bucket);
            metadataMap.put("objectName", objectName);

            for (Document document : documents) {
                if (document != null) {
                    Map<String, Object> documentMetadata = document.getMetadata();
                    documentMetadata.putAll(metadataMap);
                }
            }

            return documents;
        } catch (Exception e) {
            log.error("从MinIO提取文件失败: {}", objectName, e);
            return new ArrayList<>();
        }
    }
}

