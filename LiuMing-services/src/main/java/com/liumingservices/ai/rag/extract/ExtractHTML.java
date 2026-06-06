package com.liumingservices.ai.rag.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ExtractHTML {

    /**
     * 从HTML页面中提取文本数据
     */
    public List<org.springframework.ai.document.Document> extractFromUrl(String url) {
        log.info("开始从URL提取数据: {}", url);
        try {
            Document doc = Jsoup.connect(url).get();
            // 提取正文内容，这里简单提取body文本，实际可根据需求精细化
            String text = doc.body().text();
            
            org.springframework.ai.document.Document springAiDoc = new org.springframework.ai.document.Document(text);
            springAiDoc.getMetadata().put("source", "html");
            springAiDoc.getMetadata().put("url", url);
            springAiDoc.getMetadata().put("title", doc.title());
            
            List<org.springframework.ai.document.Document> results = new ArrayList<>();
            results.add(springAiDoc);
            return results;
        } catch (IOException e) {
            log.error("从URL提取数据失败: {}", url, e);
            return new ArrayList<>();
        }
    }
}

