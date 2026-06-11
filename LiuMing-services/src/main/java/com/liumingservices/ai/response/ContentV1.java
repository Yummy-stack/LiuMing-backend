package com.liumingservices.ai.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "LLM Input 映射实体类")
public class ContentV1 {
    @ApiModelProperty(value = "大模型输出的内容")
    private String content;
}
