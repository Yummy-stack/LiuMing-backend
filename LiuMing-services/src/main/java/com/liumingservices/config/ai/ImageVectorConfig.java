package com.liumingservices.config.ai;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ImageVectorConfig {

    @Bean
    @ConditionalOnMissingBean(QdrantClient.class)
    public QdrantClient qdrantClient(
            @Value("${spring.ai.vectorstore.qdrant.host}") String host,
            @Value("${spring.ai.vectorstore.qdrant.port}") int port
    ) {
        return new QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build());
    }
}
