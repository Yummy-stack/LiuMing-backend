package com.liumingservices.ai.advisors;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

@Slf4j
public class MemoryRQAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    @NotNull
    @Override
    public AdvisedResponse aroundCall(@NotNull AdvisedRequest advisedRequest, @NotNull CallAroundAdvisorChain chain) {
        return null;
    }

    @NotNull
    @Override
    public Flux<AdvisedResponse> aroundStream(@NotNull AdvisedRequest advisedRequest, @NotNull StreamAroundAdvisorChain chain) {
        return null;
    }

    @NotNull
    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        return null;
    }

    private void after() {

    }
}
