package com.liumingmodel.bo.ai;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "接收ai对话响应的实体类")
public class ActorFilms implements Serializable {

    @ApiModelProperty(value = "演员")
    private String actor;

    @ApiModelProperty(value = "黑料")
    private List<String> scandals;
}
