package com.liumingservices.ai.chat;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.liumingmodel.dto.qwen.CallQwenDto;
import com.liumingmodel.dto.qwen.StreamQwenDto;
import com.liumingservices.ai.advisors.HybridQuestionAnswerAdvisor;
import com.liumingservices.ai.advisors.LoggerAdvisor;
import com.liumingservices.ai.advisors.ReReadingAdvisor;
import com.liumingservices.ai.memory.RQMemory;
import com.liumingservices.ai.rag.service.HybridSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class ChatQwen {
    private final DashScopeChatModel dashscopeChatModel;

    private final ChatClient chatClient;

    private final ToolCallback[] allTools;

    private final VectorStore vectorStore;

    private final RQMemory rqMemory;

    private final HybridSearchService hybridSearchService;

    public ChatQwen(DashScopeChatModel dashscopeChatModel, ToolCallback[] allTools,
                    VectorStore vectorStore, RQMemory rqMemory,
                    HybridSearchService hybridSearchService
    ) {
        this.dashscopeChatModel = dashscopeChatModel;
        this.allTools = allTools;
        this.vectorStore = vectorStore;
        this.rqMemory = rqMemory;
        this.hybridSearchService = hybridSearchService;
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem("你是一名AI应用开发工程师")
                .defaultAdvisors(
                        new LoggerAdvisor(),
                        new ReReadingAdvisor()
                )
                .build();
    }

    public String CallChat(CallQwenDto callQwenDto) {
        // TODO 暂时写死ConversationId
        callQwenDto.setConversationId("testConversationId");
        if (callQwenDto == null || StrUtil.isBlank(callQwenDto.getUserMessage()) || StrUtil.isBlank(callQwenDto.getConversationId())) {
            throw new RuntimeException("参数为空");
        }

        String userMessage = callQwenDto.getUserMessage();
        String conversationId = callQwenDto.getConversationId();

        log.info("开始同步对话, conversationId: {}, message: {}", conversationId, userMessage);

        HashMap<String, Object> specParamsMap = new HashMap<>();
        specParamsMap.put(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId);
        specParamsMap.put(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10);

        String content = this.chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec.params(specParamsMap))
                .advisors(new MessageChatMemoryAdvisor(rqMemory))
                .advisors(new HybridQuestionAnswerAdvisor(hybridSearchService))
                .tools(allTools)
                .call()
                .content();

        return content;
    }

    public Flux<String> StreamChat(StreamQwenDto streamQwenDto) {
        // TODO 暂时写死ConversationId
        streamQwenDto.setConversationId("testConversationId");
        if (streamQwenDto == null || StrUtil.isBlank(streamQwenDto.getUserMessage()) || StrUtil.isBlank(streamQwenDto.getConversationId())) {
            return Flux.error(new RuntimeException("参数为空"));
        }

        String userMessage = streamQwenDto.getUserMessage();
        String conversationId = streamQwenDto.getConversationId();

        log.info("开始流式对话, conversationId: {}, message: {}", conversationId, userMessage);

        Flux<String> stringFlux = this.chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MessageChatMemoryAdvisor(rqMemory))
                .advisors(new HybridQuestionAnswerAdvisor(hybridSearchService))
                .tools(allTools)
                .stream()
                .content();

        return stringFlux;
    }
}
