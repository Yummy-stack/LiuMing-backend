package com.liumingservices.ai.advisors;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@Slf4j
public class LoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @NotNull
    @Override
    public AdvisedResponse aroundCall(@NotNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        log.info("调用了AI：{}", advisedRequest.userText());

        AdvisedResponse loggerAdvisorResponse = chain.nextAroundCall(advisedRequest);

        String text = Optional.of(loggerAdvisorResponse)
                .map(AdvisedResponse::response)
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .orElseThrow();

        log.info("AI输出的内容为：{}", text);
        return loggerAdvisorResponse;
    }

    @NotNull
    @Override
    public Flux<AdvisedResponse> aroundStream(@NotNull AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        Flux<AdvisedResponse> advisedResponseFlux = chain.nextAroundStream(advisedRequest);
        return advisedResponseFlux;
    }

    @NotNull
    @Override
    public String getName() {
        String advisorName = "LoggerAdvisor";
        return advisorName;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        log.info("\n调用了AI：{}", advisedRequest.userText());
        return advisedRequest;
    }

    private void after(AdvisedResponse loggerAdvisorResponse) {
        String text = Optional.of(loggerAdvisorResponse)
                .map(AdvisedResponse::response)
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .orElseThrow();
        log.info("\nAI输出的内容为：{}", text);
    }
}
