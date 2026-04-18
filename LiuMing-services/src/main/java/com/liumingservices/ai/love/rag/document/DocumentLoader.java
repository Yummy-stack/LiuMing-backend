package com.liumingservices.ai.love.rag.document;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    public List<Document> loadLoveMarkDown() {
        ArrayList<Document> documentArrayList = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                if (resource == null || !resource.exists()) {
                    throw new RuntimeException("读取到的文件为空或不存在");
                }
                if (resource.getFilename() == null) {
                    throw new RuntimeException("文件的文件名参数为空");
                }
                if (!resource.getFilename().endsWith(".md")) {
                    throw new RuntimeException("读取到的文件不为.md格式");
                }
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", resource.getFilename())
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                documentArrayList.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("读取的文件资源目录不存在");
            throw new RuntimeException(e.getMessage());
        }
        return documentArrayList;
    }

    public List<Document> loadCloudLoveRAG() {
        var dashScopeApi = new DashScopeApi(dashscopeApiKey);
        DocumentRetriever retriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName("你的知识库名称")
                        .build());
        List<Document> documentList = retriever.retrieve(new Query("谁是鱼皮"));
        return documentList;
    }
}
