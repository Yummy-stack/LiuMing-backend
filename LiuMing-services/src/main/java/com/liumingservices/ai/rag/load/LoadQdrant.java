package com.liumingservices.ai.rag.load;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoadQdrant {

    private final VectorStore vectorStore;

    /**
     * 将向量化后的文本加载到Qdrant中
     */
    public void load(List<Document> documents) {
        log.info("开始向Qdrant加载数据，文档数量: {}", documents.size());
        try {
            vectorStore.add(documents);
            log.info("成功加载数据到Qdrant");
        } catch (Exception e) {
            log.error("加载数据到Qdrant失败", e);
        }
    }
}

