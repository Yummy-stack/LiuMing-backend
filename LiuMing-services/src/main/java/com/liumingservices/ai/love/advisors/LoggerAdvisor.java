package com.liumingservices.ai.love.advisors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Slf4j
public class LoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        log.info("\n调用了AI：{}",advisedRequest.userText());

        AdvisedResponse loggerAdvisorResponse = chain.nextAroundCall(advisedRequest);
        String text = Optional.ofNullable(loggerAdvisorResponse)
                .map(AdvisedResponse::response)
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .orElseThrow();

        log.info("\nAI输出的内容为：{}",text);
        return loggerAdvisorResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        Flux<AdvisedResponse> advisedResponseFlux = chain.nextAroundStream(advisedRequest);
        return advisedResponseFlux;
    }

    @Override
    public String getName() {
        String advisorName = "LoggerAdvisor";
        return advisorName;
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
