package com.liumingservices.ai.prompt.service;

public interface PromptRefineService {
    /**
     * 书面化提示词
     *
     * @param userPrompt 提示词
     * @return 书面化后的提示词
     */
    String FormalizationStrategy(String userPrompt);

    /**
     * 精简化提示词
     *
     * @param userPrompt 提示词
     * @return 精简化后的提示词
     */
    String ConcisenessStrategy(String userPrompt);
}
