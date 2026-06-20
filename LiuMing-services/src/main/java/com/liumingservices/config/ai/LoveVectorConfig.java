package com.liumingservices.config.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LoveVectorConfig {
    // 暂时注释掉，让 Qdrant VectorStore 自动配置生效
    /*
    private final DocumentLoader documentLoader;

    @Bean
    public VectorStore loveVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        List<Document> documentList = documentLoader.loadLoveMarkDown();
        simpleVectorStore.add(documentList);

        return simpleVectorStore;
    }
    */
}
