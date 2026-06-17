package com.liumingservices.ai.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenContextManager {

    private final TokenCountEstimator tokenCountEstimator;

    public List<Message> cropByToken(List<Message> cropMessages, Integer tokenBudget) {
        log.info("开始按照tokenBudget裁剪：{}", tokenBudget);
        if (cropMessages == null || tokenBudget == 0) {
            throw new RuntimeException("请输入要裁剪的列表，和最大Token数");
        }

        ArrayList<Message> cropByTokenMessages = new ArrayList<>();
        Integer resToken = tokenBudget;

        List<Message> systemMessages = cropMessages.stream()
                .filter(m -> m.getMessageType() == MessageType.SYSTEM)
                .toList();

        if (!systemMessages.isEmpty()) {
            cropByTokenMessages.addAll(systemMessages);
            for (Message systemMessage : systemMessages) {
                int systemMessageToken = tokenCountEstimator.estimate(systemMessage.getText());
                if (resToken - systemMessageToken >= 0) {
                    cropByTokenMessages.add(0, systemMessage);
                    resToken = resToken - systemMessageToken;
                } else {
                    String exceptionMessage = String.format("SystemMessage都无法存储，Token阈值太低：{}", tokenBudget);
                    throw new RuntimeException(exceptionMessage);
                }
            }
        }

        for (int i = cropMessages.size() - 1; i >= 0; i--) {
            Message message = cropMessages.get(i);
            if (message == null) {
                log.error("消息为空，编号为：{}", i);
                break;
            }
            Integer messageToken = tokenCountEstimator.estimate(message.getText());
            if (resToken - messageToken >= 0) {
                cropByTokenMessages.add(0, message);
                resToken = resToken - messageToken;
            }
        }

        return cropByTokenMessages;
    }

}
