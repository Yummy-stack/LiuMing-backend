package com.liumingservices.controller.api.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/love-app")
@Slf4j
public class LoveAppController {

    @PostMapping(value = "/chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> loveAppChat(String userPrompt,String chatId) {
        Flux<String> stringFlux = new Flux<>() {
            @Override
            public void subscribe(CoreSubscriber<? super String> coreSubscriber) {

            }
        };
        return stringFlux;
    }
}
