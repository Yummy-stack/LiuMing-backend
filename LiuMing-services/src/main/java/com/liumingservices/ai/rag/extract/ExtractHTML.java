package com.liumingservices.ai.rag.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ExtractHTML {

    public List<Document> extractFromUrl(String url) {
        log.info("开始从URL提取数据: {}", url);
        try {
            org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
            // 提取正文内容，这里简单提取body文本，实际可根据需求精细化
            String text = doc.body().text();
            
           Document springAiDoc = new Document(text);
            springAiDoc.getMetadata().put("source", "html");
            springAiDoc.getMetadata().put("url", url);
            springAiDoc.getMetadata().put("title", doc.title());
            
            List<Document> results = new ArrayList<>();
            results.add(springAiDoc);
            return results;
        } catch (IOException e) {
            log.error("从URL提取数据失败: {}", url, e);
            return new ArrayList<>();
        }
    }
}

