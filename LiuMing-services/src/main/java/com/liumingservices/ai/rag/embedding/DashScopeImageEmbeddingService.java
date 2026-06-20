package com.liumingservices.ai.rag.embedding;

import com.liumingservices.entity.ai.response.MultimodalEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashScopeImageEmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${liuming.image-search.embedding-model:tongyi-embedding-vision-plus}")
    private String embeddingModel;

    @Value("${liuming.image-search.embedding-dimension:1024}")
    private int embeddingDimension;

    @Value("${liuming.image-search.dashscope-base-url:https://dashscope.aliyuncs.com/api/v1}")
    private String dashscopeBaseUrl;

    public List<Float> embedImage(MultipartFile file) {
        try {
            return embedImageBytes(file.getBytes(), file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("读取上传图片失败: " + e.getMessage(), e);
        }
    }

    public List<Float> embedImageBytes(byte[] imageBytes, String contentType) {
        String mimeType = resolveMimeType(contentType);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUri = "data:" + mimeType + ";base64," + base64;
        return embedImageDataUri(dataUri);
    }

    public List<Float> embedImageUrl(String imageUrl) {
        Map<String, Object> content = Map.of("image", imageUrl);
        return requestEmbedding(content);
    }

    private List<Float> embedImageDataUri(String dataUri) {
        Map<String, Object> content = Map.of("image", dataUri);
        return requestEmbedding(content);
    }

    private List<Float> requestEmbedding(Map<String, Object> contentItem) {
        String url = dashscopeBaseUrl + "/services/embeddings/multimodal-embedding/multimodal-embedding";

        Map<String, Object> input = Map.of("contents", List.of(contentItem));
        Map<String, Object> parameters = Map.of("dimension", embeddingDimension);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", embeddingModel);
        requestBody.put("input", input);
        requestBody.put("parameters", parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<MultimodalEmbeddingResponse> response = restTemplate.postForEntity(
                    url, entity, MultimodalEmbeddingResponse.class
            );
            MultimodalEmbeddingResponse body = response.getBody();
            if (body == null || body.getOutput() == null
                    || body.getOutput().getEmbeddings() == null
                    || body.getOutput().getEmbeddings().isEmpty()
            ) {
                throw new RuntimeException("DashScope 返回空的图像向量");
            }
            List<Float> embedding = body.getOutput().getEmbeddings().get(0).getEmbedding();
            if (embedding == null || embedding.isEmpty()) {
                throw new RuntimeException("DashScope 返回空的图像向量");
            }
            return embedding;
        } catch (RestClientException e) {
            log.error("调用 DashScope 图像 embedding 失败", e);
            throw new RuntimeException("图像特征提取失败: " + e.getMessage(), e);
        }
    }

    private String resolveMimeType(String contentType) {
        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }
        return "image/jpeg";
    }
}
