package com.liumingservices.image;

import com.liumingmodel.vo.imagesearch.ImageVectorPayloadVo;
import com.liumingservices.config.ai.ImageVectorInitializer;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageVectorService {

    private final QdrantClient qdrantClient;

    private final ImageVectorInitializer imageVectorInitializer;

    public void upsert(String pointId, List<Float> vector, ImageVectorPayloadVo payload) {
        try {
            String collectionName = imageVectorInitializer.getCollectionName();

            Points.PointStruct point = Points.PointStruct.newBuilder()
                    .setId(id(UUID.fromString(pointId)))
                    .setVectors(vectors(vector))
                    .putPayload("id", value(payload.getId()))
                    .putPayload("name", value(payload.getName()))
                    .putPayload("description", value(nullToEmpty(payload.getDescription())))
                    .putPayload("imageUrl", value(payload.getImageUrl()))
                    .build();

            Points.UpdateResult updateResult = qdrantClient.upsertAsync(collectionName, List.of(point)).get();
            log.info("图像向量入库成功, id={}, name={}", payload.getId(), payload.getName());
        } catch (Exception e) {
            log.error("图像向量入库失败, id={}", payload.getId(), e);
            throw new RuntimeException("图像向量入库失败: " + e.getMessage(), e);
        }
    }

    public List<ImageVectorPayloadVo> search(List<Float> queryVector, int topK) {
        try {
            String collectionName = imageVectorInitializer.getCollectionName();
            Points.SearchPoints searchPoint = Points.SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(queryVector)
                    .setLimit(topK)
                    .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                    .build();

            List<Points.ScoredPoint> scoredPoints = qdrantClient.searchAsync(searchPoint).get();

            List<ImageVectorPayloadVo> results = new ArrayList<>();
            if (scoredPoints != null) {
                for (Points.ScoredPoint scoredPoint : scoredPoints) {
                    results.add(toPayloadVo(scoredPoint));
                }
            } else {
                throw new RuntimeException("scoredPoints 参数为空");
            }
            log.info("图像相似度检索完成, topK={}, resultCount={}", topK, results.size());
            return results;
        } catch (Exception e) {
            log.error("图像相似度检索失败", e);
            throw new RuntimeException("图像相似度检索失败: " + e.getMessage(), e);
        }
    }

    private ImageVectorPayloadVo toPayloadVo(Points.ScoredPoint scoredPoint) {
        Map<String, JsonWithInt.Value> payloadMap = scoredPoint.getPayloadMap();

        ImageVectorPayloadVo imageVectorPayloadVo = ImageVectorPayloadVo.builder()
                .id(getPayloadString(payloadMap, "id"))
                .name(getPayloadString(payloadMap, "name"))
                .description(getPayloadString(payloadMap, "description"))
                .imageUrl(getPayloadString(payloadMap, "imageUrl"))
                .score(scoredPoint.getScore())
                .build();

        return imageVectorPayloadVo;
    }

    private String getPayloadString(Map<String, JsonWithInt.Value> payloadMap, String key) {
        JsonWithInt.Value val = payloadMap.get(key);
        if (val == null) {
            return null;
        }
        return val.getStringValue();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
