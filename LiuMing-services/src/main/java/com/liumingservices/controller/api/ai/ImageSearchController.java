package com.liumingservices.controller.api.ai;

import com.liumingcommon.BaseResponse;
import com.liumingcommon.utils.ResultUtils;
import com.liumingmodel.vo.imagesearch.ImageVectorPayloadVo;
import com.liumingservices.image.ImageIndexService;
import com.liumingservices.image.ImageSearchService;
import com.liumingservices.job.result.SyncResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "以图搜图")
@RestController
@RequestMapping(value = "/image-search")
@Slf4j
@RequiredArgsConstructor
public class ImageSearchController {

    private final ImageSearchService imageSearchService;

    private final ImageIndexService imageIndexService;

    @ApiOperation(value = "以图搜图")
    @PostMapping(value = "/search")
    public BaseResponse<?> searchByImage(
            @ApiParam(value = "搜索图片", required = true)
            @RequestPart(value = "file") MultipartFile file,
            @ApiParam(value = "返回结果数量", example = "10")
            @RequestParam(value = "topK", defaultValue = "10") int topK) {
        try {
            List<ImageVectorPayloadVo> results = imageSearchService.searchByImage(file, topK);
            return ResultUtils.success(results);
        } catch (IllegalArgumentException e) {
            return ResultUtils.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("以图搜图失败", e);
            return ResultUtils.error(500, "以图搜图失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "手动全量同步图像向量")
    @PostMapping(value = "/sync")
    public BaseResponse<?> syncAllImages() {
        try {
            SyncResult syncResult = imageIndexService.syncAllEquipments();

            Map<String, Object> syncResponseMap = new HashMap<>();
            syncResponseMap.put("syncResult", syncResult.getResult());
            syncResponseMap.put("successCount", syncResult.getSuccessCount());
            syncResponseMap.put("failedCount", syncResult.getFailedCount());
            syncResponseMap.put("message", "图像向量全量同步完成");

            return ResultUtils.success(syncResponseMap);
        } catch (Exception e) {
            log.error("图像向量全量同步失败", e);
            return ResultUtils.error(500, "图像向量全量同步失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "按词条ID同步单条图像向量")
    @PostMapping(value = "/sync/one")
    public BaseResponse<?> syncOneImage(
            @ApiParam(value = "词条ID", required = true)
            @RequestParam(value = "equipmentId") Long equipmentId) {
        try {
            boolean success = imageIndexService.indexEquipmentById(equipmentId);
            if (!success) {
                return ResultUtils.error(400, "该词条无可用图片，同步跳过");
            }
            return ResultUtils.success("词条图像向量同步成功");
        } catch (Exception e) {
            log.error("词条图像向量同步失败, equipmentId={}", equipmentId, e);
            return ResultUtils.error(500, "词条图像向量同步失败: " + e.getMessage());
        }
    }
}
