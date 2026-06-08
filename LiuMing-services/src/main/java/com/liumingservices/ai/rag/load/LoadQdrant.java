package com.liumingservices.ai.rag.load;

import com.google.common.collect.Lists;
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

    public void load(List<Document> documents) {
        log.info("开始向Qdrant加载数据，文档数量: {}", documents.size());

        if (documents.isEmpty()) {
            log.error("Qdrant加载数据失败，文档数量为0");
            return;
        }

        try {
            List<List<Document>> partitionDocs = Lists.partition(documents, 25);

            for (List<Document> partitionDoc : partitionDocs) {
                vectorStore.add(partitionDoc);
            }

            log.info("成功加载数据到Qdrant");
        } catch (Exception e) {
            log.error("加载数据到Qdrant失败", e);
        }
    }
}

