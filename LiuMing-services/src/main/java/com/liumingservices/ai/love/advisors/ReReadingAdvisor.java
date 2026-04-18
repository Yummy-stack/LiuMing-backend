package com.liumingservices.ai.love.advisors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

@Slf4j
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return null;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getOrder() {
        return 2;
    }

}
