package com.liumingservices.image;

import cn.hutool.core.util.StrUtil;
import com.liumingmodel.vo.imagesearch.ImageVectorPayloadVo;
import com.liumingservices.entity.Equipment;
import com.liumingservices.ai.manager.MinioManager;
import com.liumingservices.ai.rag.embedding.DashScopeImageEmbeddingService;
import com.liumingservices.mapper.EquipmentMapper;
import com.liumingservices.service.IEquipmentService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageIndexService {

    private final EquipmentMapper equipmentMapper;

    private final DashScopeImageEmbeddingService imageEmbeddingService;

    private final ImageVectorService loadImageVector;

    private final MinioClient minioClient;

    private final MinioManager minioManager;

    private final IEquipmentService equipmentService;

    private final ThreadPoolTaskExecutor syncEquVectorPool;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    // TODO待优化
    public boolean syncAllEquipments() {
        log.info("开始将装备表的图片转为向量同步到向量库中");
        // int pageNum = 1,pageSize = 1000,lastId = 0;使用游标滚轮的方式来进行分页查询
        // 将查询出来的数据，通过url获取图片数据，然后将图片 transform为Vector，通过Equipment来获取到Payload信息，通过CompletableFuture来单独开一个线程然后批量插入到Qdrant中
        //

//        log.info("图像向量全量同步完成, total={}, success={}", equipmentList.size(), successCount);
//        return successCount;
        return true;
    }

    public boolean indexEquipment(Equipment equipment) {
        if (equipment == null || StrUtil.isBlank(equipment.getFilePath())) {
            return false;
        }
        try {
            String imageUrl = buildImageUrl(equipment.getFilePath());
            List<Float> vector = embedEquipmentImage(equipment.getFilePath(), imageUrl);

            ImageVectorPayloadVo payload = ImageVectorPayloadVo.builder()
                    .id(StrUtil.toString(equipment.getId()))
                    .name(equipment.getName())
                    .description(equipment.getMainInfo())
                    .imageUrl(imageUrl)
                    .build();

            String pointId = buildPointId(equipment.getId());
            loadImageVector.upsert(pointId, vector, payload);
            return true;
        } catch (Exception e) {
            log.error("词条图像入库失败, id={}, name={}", equipment.getId(), equipment.getName(), e);
            return false;
        }
    }

    public boolean indexEquipmentById(Long equipmentId) {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("词条不存在: " + equipmentId);
        }
        return indexEquipment(equipment);
    }

    private List<Float> embedEquipmentImage(String filePath, String imageUrl) throws Exception {
        if (filePath.startsWith("http")) {
            return imageEmbeddingService.embedImageUrl(imageUrl);
        }
        byte[] imageBytes = downloadFromMinio(filePath);
        String contentType = resolveContentType(filePath);
        return imageEmbeddingService.embedImageBytes(imageBytes, contentType);
    }

    private byte[] downloadFromMinio(String filePath) throws Exception {
        String objectName = extractObjectName(filePath);
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectName).build()
        )) {
            return inputStream.readAllBytes();
        }
    }

    private String resolveContentType(String filePath) throws Exception {
        String objectName = extractObjectName(filePath);
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder().bucket(bucket).object(objectName).build()
        );
        if (stat.contentType() != null) {
            return stat.contentType();
        }
        return guessContentType(objectName);
    }

    private String extractObjectName(String filePath) {
        return minioManager.resolveObjectName(filePath);
    }

    private String buildImageUrl(String filePath) {
        if (filePath.startsWith("http")) {
            return filePath;
        }
        String objectName = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        String formattedEndpoint = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        return formattedEndpoint + bucket + "/" + objectName;
    }

    private String buildPointId(Long equipmentId) {
        return UUID.nameUUIDFromBytes(("equipment-image-" + equipmentId).getBytes()).toString();
    }

    private String guessContentType(String objectName) {
        String lower = objectName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}
