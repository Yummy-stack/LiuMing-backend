package com.liumingservices.image;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.liumingmodel.vo.imagesearch.ImageVectorPayloadVo;
import com.liumingservices.entity.Equipment;
import com.liumingservices.ai.manager.MinioManager;
import com.liumingservices.ai.rag.embedding.DashScopeImageEmbeddingService;
import com.liumingservices.job.result.BatchSyncResult;
import com.liumingservices.job.result.EquipmentSyncResult;
import com.liumingservices.job.result.RetrySyncResult;
import com.liumingservices.job.result.SyncResult;
import com.liumingservices.mapper.EquipmentMapper;
import com.liumingservices.service.IEquipmentService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

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

    public SyncResult syncAllEquipments() throws ExecutionException, InterruptedException {
        log.info("开始将装备表的图片转为向量同步到向量库中");

        int pageSize = 100;
        long lastId = 0L;
        int totalCount = 0;
        int successCount = 0;
        List<EquipmentSyncResult> failedResults = new ArrayList<>();

        while (true) {
//            List<Equipment> equipmentList = equipmentService.lambdaQuery()
//                    .gt(lastId > 0, Equipment::getId, lastId)
//                    .orderByAsc(Equipment::getId)
//                    .last("limit " + pageSize)
//                    .list();
            List<Equipment> equipmentList = equipmentMapper.selectListByLastId(lastId, pageSize);
            if (CollUtil.isEmpty(equipmentList)) {
                break;
            }

            totalCount += equipmentList.size();
            lastId = equipmentList.get(equipmentList.size() - 1).getId();

            BatchSyncResult batchSyncResult = processBatch(equipmentList);
            successCount += batchSyncResult.getSuccessCount();
            failedResults.addAll(batchSyncResult.getFailedResults());
        }

        if (CollUtil.isNotEmpty(failedResults)) {
            log.warn("图像向量异步同步存在失败记录，开始执行串行重试, failCount = {}", failedResults.size());
            RetrySyncResult retrySyncResult = retryFailedResults(failedResults);
            successCount += retrySyncResult.getRetrySuccessCount();
            failedResults = retrySyncResult.getFinalFailedResults();
        }

        String fallbackFilePath = null;
        if (CollUtil.isNotEmpty(failedResults)) {
            fallbackFilePath = persistFailedResults(failedResults);
            log.error("图像向量同步仍有失败记录, failCount = {}, fallbackFile = {}", failedResults.size(), fallbackFilePath);
        }

        log.info("图像向量全量同步完成, total = {}, success = {}, fail = {}", totalCount, successCount, totalCount - successCount);

        SyncResult syncResult = SyncResult.builder()
                .result(totalCount == successCount)
                .successCount(successCount)
                .failedCount(totalCount - successCount)
                .build();

        return syncResult;
    }

    public boolean indexEquipment(Equipment equipment) {
        return doIndexEquipment(equipment).isSuccess();
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
                GetObjectArgs.builder().bucket(bucket).object(objectName).build())) {
            return inputStream.readAllBytes();
        }
    }

    private String resolveContentType(String filePath) throws Exception {
        String objectName = extractObjectName(filePath);
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder().bucket(bucket).object(objectName).build());
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
        return equipmentId.toString();
    }

    private BatchSyncResult processBatch(List<Equipment> equipmentList) throws ExecutionException, InterruptedException {
        int asyncBatchSize = Math.max(1, syncEquVectorPool.getMaxPoolSize() - 2);
        AtomicInteger batchSuccessCount = new AtomicInteger();
        List<EquipmentSyncResult> failedResults = new ArrayList<>();

        List<List<Equipment>> BatchEquipments = Lists.partition(equipmentList, asyncBatchSize);
        for (List<Equipment> currentBatch : BatchEquipments) {
            ArrayList<CompletableFuture<EquipmentSyncResult>> imgToVecTasks = new ArrayList<>();
            for (Equipment equipment : currentBatch) {
                try {
                    CompletableFuture<EquipmentSyncResult> imgToVecTask = CompletableFuture.supplyAsync(() -> doIndexEquipment(equipment), syncEquVectorPool);
                    imgToVecTasks.add(imgToVecTask);
                } catch (Exception e) {
                    log.error("同步异常：{}", equipment.getId());
                    throw new RuntimeException(e);
                }
            }
            CompletableFuture.allOf(imgToVecTasks.toArray(new CompletableFuture[0])).join();

            for (CompletableFuture<EquipmentSyncResult> imgToVecTask : imgToVecTasks) {
                EquipmentSyncResult equipmentSyncResult = imgToVecTask.get();
                if (equipmentSyncResult.isSuccess()) {
                    batchSuccessCount.incrementAndGet();
                } else {
                    failedResults.add(equipmentSyncResult);
                }
            }
        }

        return new BatchSyncResult(batchSuccessCount.get(), failedResults);
    }

    private RetrySyncResult retryFailedResults(List<EquipmentSyncResult> failedResults) {
        int retrySuccessCount = 0;
        List<EquipmentSyncResult> finalFailedResults = new ArrayList<>();

        for (EquipmentSyncResult failedResult : failedResults) {
            EquipmentSyncResult retryResult = doIndexEquipment(failedResult.getEquipment());
            if (retryResult.isSuccess()) {
                retrySuccessCount++;
            } else {
                finalFailedResults.add(retryResult);
            }
        }

        return new RetrySyncResult(retrySuccessCount, finalFailedResults);
    }

    private String persistFailedResults(List<EquipmentSyncResult> failedResults) {
        String dirPath = System.getProperty("user.dir") + "/logs/image-index-failures";
        FileUtil.mkdir(dirPath);
        String filePath = dirPath + "/image-index-failed-"
                + DateUtil.formatDateTime(DateUtil.date()).replace(" ", "-").replace(":", "") + ".log";

        List<String> lines = new ArrayList<>();
        lines.add("图像向量同步失败兜底文件");
        lines.add("failCount=" + failedResults.size());
        lines.add("========================================");
        for (EquipmentSyncResult failedResult : failedResults) {
            Equipment equipment = failedResult.getEquipment();
            lines.add("equipmentId=" + (equipment == null ? "" : equipment.getId())
                    + ", name=" + (equipment == null ? "" : equipment.getName())
                    + ", filePath=" + (equipment == null ? "" : equipment.getFilePath())
                    + ", reason=" + failedResult.getErrorMessage());
        }
        FileUtil.writeLines(lines, filePath, StandardCharsets.UTF_8);
        return filePath;
    }

    //写入到equipment_sync_failed表中
    private String persistFailedResultsV2(List<EquipmentSyncResult> failedResults) {
        return null;
    }

    private EquipmentSyncResult doIndexEquipment(Equipment equipment) {
        if (equipment == null) {
            return EquipmentSyncResult.fail(null, "词条为空");
        }
        if (StrUtil.isBlank(equipment.getFilePath())) {
            return EquipmentSyncResult.fail(equipment, "图片路径为空");
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
            return EquipmentSyncResult.success(equipment);
        } catch (Exception e) {
            log.error("词条图像入库失败, id={}, name={}", equipment.getId(), equipment.getName(), e);
            return EquipmentSyncResult.fail(equipment, e.getMessage());
        }
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
