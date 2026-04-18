package com.liumingservices.ai.love.config;

import com.liumingservices.ai.love.rag.document.DocumentLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LoveVectorConfig {
    private final DocumentLoader documentLoader;

    @Bean
    public VectorStore loveVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        List<Document> documentList = documentLoader.loadLoveMarkDown();
        simpleVectorStore.add(documentList);

        return simpleVectorStore;
    }

}
