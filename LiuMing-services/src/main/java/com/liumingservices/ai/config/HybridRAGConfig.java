package com.liumingservices.ai.config;

import com.liumingservices.ai.advisors.HybridQuestionAnswerAdvisor;
import com.liumingservices.ai.rag.service.HybridSearchService;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HybridRAGConfig {

    @Bean
    public Advisor hybridRAGAdvisor(HybridSearchService hybridSearchService) {
        return new HybridQuestionAnswerAdvisor(hybridSearchService);
    }
}
