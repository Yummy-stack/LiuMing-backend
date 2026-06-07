package com.liumingservices.ai.rag.load;

import com.liumingservices.ai.rag.document.es.EsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoadElasticsearch {

    private final ElasticsearchOperations elasticsearchOperations;

    public void load(List<Document> documents) {
        log.info("开始向 Elasticsearch 加载数据，文档数量: {}", documents.size());
        try {
            List<EsDocument> esDocuments = documents.stream()
                    .map(doc -> EsDocument.builder()
                            .id(doc.getId())
                            .content(doc.getText())
                            .metadata(doc.getMetadata())
                            .build())
                    .collect(Collectors.toList());
            
            elasticsearchOperations.save(esDocuments);
            log.info("成功加载数据到 Elasticsearch：{}条", documents.size());
        } catch (Exception e) {
            log.error("加载数据到 Elasticsearch 失败", e);
        }
    }
}
