package com.liumingservices.controller.api.ai;

import com.liumingcommon.BaseResponse;
import com.liumingcommon.utils.ResultUtils;
import com.liumingservices.ai.rag.service.ETLService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "RAG中的ETL")
@RestController
@RequestMapping("/etl")
@Slf4j
@RequiredArgsConstructor
public class ETLController {

    private final ETLService etlService;

    @ApiOperation(value = "触发全量ETL同步")
    @PostMapping(value = "/run")
    public BaseResponse<?> runETL() {
        try {
            etlService.runETL();
            return ResultUtils.success("ETL同步任务启动成功");
        } catch (Exception e) {
            log.error("ETL同步任务失败", e);
            return ResultUtils.error(500, "ETL同步任务失败: " + e.getMessage());
        }
    }
}
