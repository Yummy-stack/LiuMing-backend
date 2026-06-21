package com.liumingservices.job.sync.ai;

import com.liumingservices.image.ImageIndexService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ToQdrantJob {

    private final ImageIndexService imageIndexService;

    @XxlJob(value = "imageToQdrantJob")
    public void imageToQdrantJob() throws ExecutionException, InterruptedException {
        log.info("开始执行定时任务：图片同步到向量数据库中");
        XxlJobHelper.log("开始执行定时任务：图片同步到向量数据库中");

        imageIndexService.syncAllEquipments();

        log.info("定时任务执行完成：图片同步到向量数据库中");
        XxlJobHelper.log("定时任务执行完成：图片同步到向量数据库中");
    }


}
