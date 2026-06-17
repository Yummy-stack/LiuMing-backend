package com.liumingservices.ai.memory;

import com.liumingservices.ai.manager.TokenContextManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Content;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.liumingcommon.constants.ai.memory.MemoryConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class RQMemory implements ChatMemory {

    private final RedisTemplate<String, Object> redisTemplate;

    private final VectorStore vectorStore;

    private final TokenContextManager tokenContextManager;

    private final ThreadPoolTaskExecutor memoryThreadPool;

    @Override
    public void add(String conversationId, List<Message> messages) {
        log.info("会话记忆机制触发，开始存储上文信息：{}", conversationId);

        if (messages == null || messages.isEmpty()) {
            return;
        }
        ArrayList<CompletableFuture<Void>> msgAddTasks = new ArrayList<>();

        CompletableFuture<Void> redisAddTask = CompletableFuture.runAsync(() -> {
            String key = REDIS_KEY_PREFIX + conversationId;
            for (Message message : messages) {
                if (message != null) {
                    String messageJson = String.format("{\"type\":\"%s\",\"content\":\"%s\"}",
                            message.getMessageType().getValue(),
                            message.getText().replace("\"", "\\\""));
                    redisTemplate.opsForList().rightPush(key, messageJson);
                }
            }
            redisTemplate.expire(key, MEMORY_EXPIRATION_DAYS, TimeUnit.DAYS);
        }, memoryThreadPool);
        msgAddTasks.add(redisAddTask);

        CompletableFuture<Void> qdrantAddTask = CompletableFuture.runAsync(() -> {
            List<Document> documents = messages.stream()
                    .map(msg -> constructDoc(msg, conversationId))
                    .collect(Collectors.toList());

            if (!documents.isEmpty()) {
                vectorStore.add(documents);
            }
        }, memoryThreadPool);
        msgAddTasks.add(qdrantAddTask);

        CompletableFuture.allOf(msgAddTasks.toArray(new CompletableFuture[]{})).join();

    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        log.info("会话记忆机制触发，开始获取上文信息 conversation: {}, lastN: {}", conversationId, lastN);

        String key = REDIS_KEY_PREFIX + conversationId;
        List<Object> redisResult = redisTemplate.opsForList().range(key, -lastN, -1);
        if (redisResult == null) {
            return new ArrayList<Message>();
        }

        List<String> redisTexts = redisResult.stream().map(Object::toString).toList();
        List<Message> redisMessages = new ArrayList<>();
        for (String json : redisTexts) {
            if (json.contains("\"USER\"")) {
                String content = extractContent(json);
                redisMessages.add(new UserMessage(content));
            } else if (json.contains("\"ASSISTANT\"")) {
                String content = extractContent(json);
                redisMessages.add(new AssistantMessage(content));
            }
        }

        Set<String> redisMessageSet = redisMessages.stream()
                .map(Content::getText)
                .collect(Collectors.toSet());

        List<Message> finalContext = new ArrayList<>(redisMessages);

        SearchRequest memorySearchRequest = SearchRequest.builder()
                .query(conversationId)
                .topK(lastN)
                .filterExpression("conversationId == '" + conversationId + "'")
                .build();
        List<Document> semanticDocs = vectorStore.similaritySearch(memorySearchRequest);

        List<Message> semanticMessages = toMessages(semanticDocs);
        for (Message qdrantMessage : semanticMessages) {
            if (qdrantMessage == null) {
                throw new RuntimeException("消息为空");
            }
            String qdrantMessageText = qdrantMessage.getText();
            if (!redisMessageSet.contains(qdrantMessage)) {
                String historyQdrantMessage = String.format("[历史相关记忆]：{}", qdrantMessageText);
                if (MessageType.USER.equals(qdrantMessage.getMessageType())) {
                    UserMessage userMessage = new UserMessage(historyQdrantMessage);
                    finalContext.add(0, userMessage);
                }
                if (MessageType.ASSISTANT.equals(qdrantMessage.getMessageType())) {
                    AssistantMessage assistantMessage = new AssistantMessage(historyQdrantMessage);
                    finalContext.add(0, assistantMessage);
                }
                if (MessageType.SYSTEM.equals(qdrantMessage.getMessageType())) {
                    SystemMessage systemMessage = new SystemMessage(historyQdrantMessage);
                    finalContext.add(0, systemMessage);
                }
                redisMessageSet.add(qdrantMessageText);
            }
        }

        List<Message> croppedByTokenMessages = tokenContextManager.cropByToken(finalContext, MEMORY_BUDGET_TOKEN);

        return croppedByTokenMessages;
    }

    @Override
    public void clear(String conversationId) {
        log.info("开始清除会话记忆 conversation: {}", conversationId);
        String key = REDIS_KEY_PREFIX + conversationId;
        redisTemplate.delete(key);
        vectorStore.delete("conversationId == '" + conversationId + "'");
    }

    private String extractContent(String json) {
        int start = json.indexOf("\"content\":\"") + 11;
        int end = json.lastIndexOf("\"}");
        return json.substring(start, end).replace("\\\"", "\"");
    }

    private List<Message> toMessages(List<Document> semanticDocs) {
        if (semanticDocs == null || semanticDocs.isEmpty()) {
            throw new RuntimeException("参数错误");
        }

        List<Message> messages = semanticDocs.stream()
                .sorted(Comparator.comparingInt(this::convertToSortNum))
                .map(this::constructMsg)
                .collect(Collectors.toList());

        return messages;
    }

    private Document constructDoc(Message message, String conversationId) {
        if (message == null || conversationId == null) {
            throw new RuntimeException("参数错误");
        }
        HashMap<String, Object> medataData = new HashMap<>();
        medataData.put("conversationId", conversationId);
        medataData.put("type", message.getMessageType());

        Document document = Document.builder()
                .text(message.getText())
                .metadata(medataData)
                .build();

        return document;
    }

    private Message constructMsg(Document document) {
        if (document == null) {
            throw new RuntimeException("参数错误");
        }
        Map<String, Object> documentMetadata = document.getMetadata();
        String role = documentMetadata.get("role").toString();
        String content = document.getText();

        Message message = switch (role) {
            case "user" -> new UserMessage(content);
            case "assistant" -> new AssistantMessage(content);
            case "system" -> new SystemMessage(content);
            default -> new UserMessage(content); // fallback
        };

        return message;
    }

    private Integer convertToSortNum(Document document) {
        if (document == null) {
            throw new RuntimeException("参数错误");
        }
        Map<String, Object> documentMetadata = document.getMetadata();
        Object number = documentMetadata.getOrDefault("turn_index", "0");
        int sortNum = Integer.parseInt(number.toString());
        return sortNum;
    }
}

