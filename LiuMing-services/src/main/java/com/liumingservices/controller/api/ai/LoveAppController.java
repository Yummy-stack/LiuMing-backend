package com.liumingservices.controller.api.ai;

import com.liumingmodel.enums.error.ErrorCode;
import com.liumingservices.ai.love.chat.LoveAiChat;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
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
    @Resource
    private LoveAiChat loveAiChat;

    @ApiOperation(value = "LLM对话接口 - 流式输出")
    @PostMapping(value = "/chat/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> loveAppChatStream(String userPrompt,String chatId) {
        Flux<String> messageFlux = loveAiChat.doStreamWithRAG(userPrompt, chatId);
        if (messageFlux == null) {
            throw new RuntimeException(ErrorCode.SYSTEM_ERROR.getMessage());
        }
        return messageFlux;
    }

    @ApiOperation(value = "LLM对话接口 - 同步输出")
    @PostMapping(value = "/chat/call")
    public String loveAppChatCall(String userPrompt,String chatId) {
        String llmResponse = loveAiChat.doCallWithCloudRAG(userPrompt, chatId);
        if (llmResponse == null) {
            throw new RuntimeException(ErrorCode.SYSTEM_ERROR.getMessage());
        }
        return llmResponse;
    }
}
