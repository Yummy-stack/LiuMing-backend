package com.liumingmodel.dto.qwen;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@ApiModel(value = "同步对话Qwen的参数")
@Data
public class CallQwenDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 718811025483943998L;

    @ApiModelProperty(value = "用户提示词")
    private String userMessage;

    @ApiModelProperty(value = "会话ID")
    private String conversationId;

}
