package com.liumingservices.ai.memory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RQMemory implements ChatMemory {

    private final StringRedisTemplate stringRedisTemplate;

    private final VectorStore vectorStore;

    private static final String REDIS_KEY_PREFIX = "liuming:memory:";

    private static final long MEMORY_EXPIRATION_DAYS = 30;

    @Override
    public void add(String conversationId, List<Message> messages) {
        log.info("=== 会话记忆开始填充上文信息 ===");
        if (messages == null || messages.isEmpty()) {
            return;
        }

        String key = REDIS_KEY_PREFIX + conversationId;
        log.info("Adding messages to memory for conversation: {}", conversationId);

        // 1. 将文本信息存入 Redis (使用 List 存储，保持时序)
        for (Message message : messages) {
            String messageJson = String.format("{\"type\":\"%s\",\"content\":\"%s\"}",
                    message.getMessageType().getValue(),
                    message.getText().replace("\"", "\\\""));
            stringRedisTemplate.opsForList().rightPush(key, messageJson);
        }
        stringRedisTemplate.expire(key, MEMORY_EXPIRATION_DAYS, TimeUnit.DAYS);

        // 2. 将 UserMessage 的向量信息存入 Qdrant (用于语义召回记忆)
        List<Document> documents = messages.stream()
                .filter(msg -> msg.getMessageType() == MessageType.USER)
                .map(msg -> {
                    Document doc = new Document(msg.getText());
                    doc.getMetadata().put("conversationId", conversationId);
                    doc.getMetadata().put("type", "memory");
                    return doc;
                })
                .collect(Collectors.toList());

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        log.info("=== 会话记忆开始获取上文信息 ===");
        String key = REDIS_KEY_PREFIX + conversationId;
        log.info("Retrieving memory for conversation: {}, lastN: {}", conversationId, lastN);

        // 1. 从 Redis 中按照滑动窗口取出最近的文本记忆 (精准时序记忆)
        List<String> redisMessages = stringRedisTemplate.opsForList().range(key, -lastN, -1);
        List<Message> result = new ArrayList<>();

        if (redisMessages != null) {
            for (String json : redisMessages) {
                // 简单解析 JSON (实际可用 Jackson)
                if (json.contains("\"USER\"")) {
                    String content = extractContent(json);
                    result.add(new UserMessage(content));
                } else if (json.contains("\"ASSISTANT\"")) {
                    String content = extractContent(json);
                    result.add(new AssistantMessage(content));
                }
            }
        }

        List<Message> finalContext = new ArrayList<>();

// 1. Redis 的最近 N 条（高优先级）
        finalContext.addAll(result);


//        // 2. 如果时序记忆不足或为了增强 RAG 效果，从 Qdrant 召回语义相关的历史对话
//        // 注意：这里根据需求，如果只要 lastN 条，通常 Redis 的时序记忆更重要
//        // 如果需要“双路召回”，可以把语义相关的也加进去，或者作为 context 传入
        if (result.size() < lastN) {
            SearchRequest memorySearchRequest = SearchRequest.builder()
                    .query(conversationId)
                    .topK(lastN)
                    .filterExpression("conversationId == '" + conversationId + "'")
                    .build();
            List<Document> semanticDocs = vectorStore.similaritySearch(memorySearchRequest);
            // 处理语义召回逻辑...

            // 2. 语义召回（去重后追加）
            List<Message> semanticMessages = toMessages(semanticDocs);

            for (Message msg : semanticMessages) {
                boolean exists = finalContext.stream()
                        .anyMatch(m -> m.getText().equals(msg.getText()));
                if (!exists) {
                    finalContext.add(msg);
                }
            }
        }
        return result;
    }

    @Override
    public void clear(String conversationId) {
        String key = REDIS_KEY_PREFIX + conversationId;
        stringRedisTemplate.delete(key);
        // 注意：Qdrant 的删除通常基于 Filter 表达式，这里如果需要完全清理，需要调用 vectorStore.delete
        log.info("Cleared memory for conversation: {}", conversationId);
    }

    private String extractContent(String json) {
        int start = json.indexOf("\"content\":\"") + 11;
        int end = json.lastIndexOf("\"}");
        return json.substring(start, end).replace("\\\"", "\"");
    }

    // 将语义召回的 Document 转为 Message
    private List<Message> toMessages(List<Document> semanticDocs) {
        return semanticDocs.stream()
                // 1. 按对话轮次排序（避免乱序）
                .sorted(Comparator.comparingInt(
                        doc -> Integer.parseInt(doc.getMetadata().getOrDefault("turn_index", "0").toString())
                ))
                .map(doc -> {
                    String role = doc.getMetadata().get("role").toString();
                    String content = doc.getText();

                    return switch (role) {
                        case "user" -> new UserMessage(content);
                        case "assistant" -> new AssistantMessage(content);
                        case "system" -> new SystemMessage(content);
                        default -> new UserMessage(content); // fallback
                    };
                })
                .collect(Collectors.toList());
    }
}
