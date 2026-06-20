package com.liumingservices.image;

import com.liumingmodel.vo.imagesearch.ImageVectorPayloadVo;
import com.liumingservices.ai.rag.embedding.DashScopeImageEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageSearchService {

    private final DashScopeImageEmbeddingService imageEmbeddingService;

    private final ImageVectorService loadImageVector;

    /**
     * 以图搜图：提取上传图片的特征向量，在 Qdrant 中做相似度检索
     */
    public List<ImageVectorPayloadVo> searchByImage(MultipartFile file, int topK) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传图片不能为空");
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("topK 必须大于 0");
        }

        log.info("开始以图搜图, fileName = {}, topK = {}", file.getOriginalFilename(), topK);
        List<Float> queryVector = imageEmbeddingService.embedImage(file);

        return loadImageVector.search(queryVector, topK);
    }
}
