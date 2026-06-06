package com.liumingservices.ai.agent;

import cn.hutool.ai.core.Message;
import com.liumingmodel.enums.agent.AgentStateEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public abstract class BaseAgent {
    private String agentName;

    private String systemPrompt;

    private String nextStepPrompt;

    private AgentStateEnum agentStateEnum = AgentStateEnum.IDLE;

    private int maxSteps = 10;

    private int currentStep = 0;

    private ChatClient chatClient;

    private List<Message> messageList = new ArrayList<>();

    public void run() {
        try {
            for (int i = 1;currentStep < maxSteps && agentStateEnum != AgentStateEnum.FINISHED; i++) {
                currentStep = i;
                agentStateEnum = AgentStateEnum.RUNNING;
                boolean stepResult = this.step();

                if (currentStep >= maxSteps) {
                    log.info("---- agent已经达到最大运行次数 ----\n");
                    agentStateEnum = AgentStateEnum.FINISHED;
                }
            }
        } catch (Exception e) {
            agentStateEnum = AgentStateEnum.ERROR;
            throw new RuntimeException(e);
        } finally {
            this.cleanup();
        }
    }

    public abstract boolean step();

    public abstract void cleanup();
}
