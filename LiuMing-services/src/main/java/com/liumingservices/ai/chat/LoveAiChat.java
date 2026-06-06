package com.liumingservices.ai.chat;


import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.liumingservices.ai.advisors.LoggerAdvisor;
import com.liumingservices.ai.tools.FileOperationTool;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
//@RequiredArgsConstructor
@Slf4j
public class LoveAiChat {
    private final ChatModel dashscopeChatModel;

//    private final VectorStore loveVectorStore;

    private final Advisor loveCloudAdvisor;

    private final Advisor hybridRAGAdvisor;

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    public LoveAiChat(DashScopeChatModel dashscopeChatModel, Advisor loveCloudAdvisor, Advisor hybridRAGAdvisor, VectorStore vectorStore) {
        this.dashscopeChatModel = dashscopeChatModel;
        this.loveCloudAdvisor = loveCloudAdvisor;
        this.hybridRAGAdvisor = hybridRAGAdvisor;
        this.vectorStore = vectorStore;
        InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory();
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(inMemoryChatMemory),
                        new LoggerAdvisor()
                )
                .defaultSystem("你是一名军事装备专家")
                .build();
    }

    public String doCallWithHybridRAG(String message, String chatId) {
        HashMap<String, Object> paramsHashMap = new HashMap<>();
        paramsHashMap.put(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10);
        paramsHashMap.put(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);

        ChatResponse chatResponse = this.chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.params(paramsHashMap))
                .advisors(hybridRAGAdvisor)
                .call()
                .chatResponse();

        if (chatResponse == null) {
            throw new RuntimeException("对话失败,AI服务未响应结果");
        }

        Generation generation = chatResponse.getResult();
        if (generation == null) {
            throw new RuntimeException("AI响应的结果为空");
        }

        AssistantMessage assistantMessage = generation.getOutput();
        if (assistantMessage == null) {
            throw new RuntimeException("AI响应的内容输出结果为空");
        }

        return assistantMessage.getText();
    }

    public String doCallChat(String message, String chatId) {
        HashMap<String, Object> paramsHashMap = new HashMap<>();
        paramsHashMap.put(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10);
        paramsHashMap.put(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);

        ChatResponse chatResponse = this.chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.params(paramsHashMap))
                .tools(new FileOperationTool())
                .call()
                .chatResponse();

        if (chatResponse == null) {
            throw new RuntimeException("对话失败,AI服务未响应结果");
        }

        Generation generation = chatResponse.getResult();
        if (generation == null) {
            throw new RuntimeException("AI响应的结果为空");
        }

        AssistantMessage assistantMessage = generation.getOutput();
        if (assistantMessage == null) {
            throw new RuntimeException("AI响应的内容输出结果为空");
        }

        String text = assistantMessage.getText();

        return text;
    }

//    public String doCallWithRAG(String message, String chatId) {
//        HashMap<String, Object> paramsHashMap = new HashMap<>();
//        paramsHashMap.put(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10);
//        paramsHashMap.put(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);
//
//        ChatResponse chatResponse = this.chatClient.prompt()
//                .user(message)
//                .advisors(advisorSpec -> advisorSpec.params(paramsHashMap))
//                .advisors(new QuestionAnswerAdvisor(loveVectorStore))
//                .call()
//                .chatResponse();
//
//        if (chatResponse == null) {
//            throw new RuntimeException("对话失败,AI服务未响应结果");
//        }
//
//        Generation generation = chatResponse.getResult();
//        if (generation == null) {
//            throw new RuntimeException("AI响应的结果为空");
//        }
//
//        AssistantMessage assistantMessage = generation.getOutput();
//        if (assistantMessage == null) {
//            throw new RuntimeException("AI响应的内容输出结果为空");
//        }
//
//        String text = assistantMessage.getText();
//
//        return text;
//    }

    public String doCallWithCloudRAG(String message, String chatId) {
        HashMap<String, Object> paramsHashMap = new HashMap<>();
        paramsHashMap.put(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10);
        paramsHashMap.put(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);

        ChatResponse chatResponse = this.chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.params(paramsHashMap))
                .advisors(loveCloudAdvisor)
                .call()
                .chatResponse();

        if (chatResponse == null) {
            throw new RuntimeException("对话失败,AI服务未响应结果");
        }

        Generation generation = chatResponse.getResult();
        if (generation == null) {
            throw new RuntimeException("AI响应的结果为空");
        }

        AssistantMessage assistantMessage = generation.getOutput();
        if (assistantMessage == null) {
            throw new RuntimeException("AI响应的内容输出结果为空");
        }

        String text = assistantMessage.getText();

        return text;
    }

//    public Flux<String> doStreamWithRAG(String message, String chatId) {
//        Flux<String> stringFlux = this.chatClient.prompt()
//                .user(message)
//                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//                .advisors(new QuestionAnswerAdvisor(loveVectorStore))
//                .stream()
//                .content();
//
//        return stringFlux;
//    }
}
