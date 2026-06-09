package com.liumingservices.ai.prompt.service.impl;

import com.liumingservices.ai.prompt.service.PromptRefineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Component;

import static com.liumingservices.ai.prompt.RefinePrompt.REFINE_PROMPT;

@Component
@Slf4j
//@RequiredArgsConstructor
public class PromptRefineServiceImpl implements PromptRefineService {

    private final ChatModel dashscopeChatModel;

    private final ChatClient chatClient;

    public PromptRefineServiceImpl(ChatModel dashscopeChatModel) {
        this.dashscopeChatModel = dashscopeChatModel;
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem("你是一名专门负责书面化提示词的专家")
                .build();
    }


    @Override
    public String FormalizationStrategy(String userPrompt) {
        if (userPrompt == null) {
            throw new IllegalArgumentException("提示词为空");
        }
        String FormalizationPrompt = REFINE_PROMPT + userPrompt;
        String FormalizedContent = chatClient.prompt()
                .user(FormalizationPrompt)
                .call()
                .content();

        return FormalizedContent;
    }

    @Override
    public String ConcisenessStrategy(String userPrompt) {
        return "";
    }
}
