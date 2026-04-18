package com.liumingservices.ai.love.agent;

import jakarta.annotation.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReactAgent{
    @Resource
    private ToolCallback[] availableTools;

    @Resource
    private ToolCallingManager toolCallingManager;

    @Resource
    private ChatOptions chatOptions;

    private ChatResponse toolCallChatResponse;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;

    }
    @Override
    public void think() {

    }

    @Override
    public void act() {

    }
}
