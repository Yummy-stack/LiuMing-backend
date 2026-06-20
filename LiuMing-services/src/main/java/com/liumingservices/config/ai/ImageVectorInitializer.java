package com.liumingservices.config.ai;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Data
@RequiredArgsConstructor
public class ImageVectorInitializer {

    private final QdrantClient qdrantClient;

    @Value("${liuming.image-search.collection-name:military_image}")
    private String collectionName;

    @Value("${liuming.image-search.embedding-dimension:1024}")
    private int embeddingDimension;

    @PostConstruct
    public void initImageCollection() {
        try {
            boolean exists = Boolean.TRUE.equals(qdrantClient.collectionExistsAsync(collectionName).get());
            if (exists) {
                log.info("图像向量集合已存在: {}", collectionName);
                return;
            }
            VectorParams vectorParams = VectorParams.newBuilder()
                    .setSize(embeddingDimension)
                    .setDistance(Distance.Cosine)
                    .build();

            qdrantClient.createCollectionAsync(collectionName, vectorParams).get();

            log.info("图像向量集合创建成功: {}, dimension={}", collectionName, embeddingDimension);
        } catch (Exception e) {
            log.error("初始化图像向量集合失败: {}", collectionName, e);
        }
    }
}
