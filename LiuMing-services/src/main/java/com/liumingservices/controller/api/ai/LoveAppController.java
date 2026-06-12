package com.liumingservices.controller.api.ai;

import com.liumingmodel.enums.error.ErrorCode;
import com.liumingservices.ai.chat.LoveAiChat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "AI对话接口")
@RestController
@RequestMapping(value = "/love-app")
@Slf4j
public class LoveAppController {
    @Resource
    private LoveAiChat loveAiChat;

    @Operation(summary = "LLM对话接口 - 同步输出（支持工具调用）")
    @PostMapping(value = "/chat/call")
    public String loveAppChatCall(@RequestParam String userPrompt, @RequestParam String chatId) {
        String llmResponse = loveAiChat.doCallChat(userPrompt, chatId);
        if (llmResponse == null) {
            throw new RuntimeException(ErrorCode.SYSTEM_ERROR.getMessage());
        }
        return llmResponse;
    }

    @Operation(summary = "军事装备问答 - 混合检索（支持工具调用）")
    @PostMapping(value = "/chat/military")
    public String militaryChat(@RequestParam String userPrompt, @RequestParam String chatId) {
        String llmResponse = loveAiChat.doCallWithHybridRAG(userPrompt, chatId);
        if (llmResponse == null) {
            throw new RuntimeException(ErrorCode.SYSTEM_ERROR.getMessage());
        }
        return llmResponse;
    }
}
