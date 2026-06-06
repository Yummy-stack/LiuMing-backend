package com.liumingservices.ai.advisors;

import com.liumingservices.ai.rag.service.HybridSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Content;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义混合检索 Advisor
 */
@Slf4j
public class HybridQuestionAnswerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private final HybridSearchService hybridSearchService;
    private final String promptTemplate;

    public HybridQuestionAnswerAdvisor(HybridSearchService hybridSearchService) {
        this(hybridSearchService, "基于以下上下文回答问题：\n\n{context}\n\n用户问题：{question}");
    }

    public HybridQuestionAnswerAdvisor(HybridSearchService hybridSearchService, String promptTemplate) {
        this.hybridSearchService = hybridSearchService;
        this.promptTemplate = promptTemplate;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        AdvisedRequest processedRequest = before(advisedRequest);
        return chain.nextAroundCall(processedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        AdvisedRequest processedRequest = before(advisedRequest);
        return chain.nextAroundStream(processedRequest);
    }

    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        String query = advisedRequest.userText();
        log.info("HybridAdvisor 开始处理查询: {}", query);
        
        // 执行混合检索
        List<Document> documents = hybridSearchService.search(query, 5);
        
        String context = documents.stream()
                .map(Content::getText)
                .collect(Collectors.joining("\n\n"));

        Map<String, Object> advisedContext = new HashMap<>(advisedRequest.adviseContext());
        advisedContext.put("rag_documents", documents);

        String fullPrompt = promptTemplate
                .replace("{context}", context)
                .replace("{question}", query);

        return AdvisedRequest.from(advisedRequest)
                .withUserText(fullPrompt)
                .withAdviseContext(advisedContext)
                .build();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getName() {
        return "";
    }
}
