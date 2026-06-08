package com.liumingservices.ai.rag.service;

import com.liumingservices.ai.rag.document.es.EsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HybridSearchService {

    private final VectorStore vectorStore;

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 混合检索：ES全文检索 + Qdrant向量检索
     * 采用双路召回策略
     */
    public List<Document> search(String queryText, int topK) {
        log.info("开始混合检索: {}", queryText);

        // 1. Qdrant 向量检索 (召回率高，语义理解强)
        SearchRequest searchRequest = SearchRequest.builder()
                .query(queryText)
                .topK(topK)
                .build();
        List<Document> vectorDocs = null;
        try {
            vectorDocs = vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.error("查询Qdrant失败");
        }

        // 2. Elasticsearch 全文检索 (准确率高，关键词匹配强)
        List<Document> fullTextDocs = searchFromEs(queryText, topK);

        // 3. 双路召回结果合并 (RRF 算法或简单去重合并)
        return mergeResults(vectorDocs, fullTextDocs, topK);
    }

    private List<Document> searchFromEs(String query, int topK) {
        Criteria criteria = new Criteria("content").contains(query);
        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
        // 设置分页
        criteriaQuery.setPageable(PageRequest.of(0, topK));

        SearchHits<EsDocument> searchHits = elasticsearchOperations.search(criteriaQuery, EsDocument.class);

        return searchHits.getSearchHits().stream()
                .map(hit -> {
                    EsDocument esDoc = hit.getContent();
                    Document doc = new Document(esDoc.getContent(), esDoc.getMetadata());
                    // 记录 ES 的分值，以便后续可能的重排序
                    doc.getMetadata().put("es_score", hit.getScore());
                    return doc;
                })
                .collect(Collectors.toList());
    }

    /**
     * 结果合并与去重 (简单策略：交集优先，按来源排序)
     */
    private List<Document> mergeResults(List<Document> vectorDocs, List<Document> fullTextDocs, int topK) {
        Map<String, Document> mergedMap = new LinkedHashMap<>();

        // 简单的 RRF (Reciprocal Rank Fusion) 思想简化版
        // 也可以直接去重合并，这里先采用去重合并，优先向量检索结果
        for (Document doc : vectorDocs) {
            mergedMap.put(doc.getId(), doc);
        }

        for (Document doc : fullTextDocs) {
            if (!mergedMap.containsKey(doc.getId())) {
                mergedMap.put(doc.getId(), doc);
            } else {
                // 如果已存在，可以累加分值或标记为双路召回
                mergedMap.get(doc.getId()).getMetadata().put("dual_recall", true);
            }
        }

        return mergedMap.values().stream()
                .limit(topK)
                .collect(Collectors.toList());
    }
}
