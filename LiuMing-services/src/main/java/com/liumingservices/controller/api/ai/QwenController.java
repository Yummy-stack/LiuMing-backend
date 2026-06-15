package com.liumingservices.controller.api.ai;

import com.liumingcommon.BaseResponse;
import com.liumingcommon.utils.ResultUtils;
import com.liumingmodel.dto.qwen.CallQwenDto;
import com.liumingmodel.dto.qwen.StreamQwenDto;
import com.liumingservices.ai.chat.ChatQwen;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Api(value = "大模型接口")
@RestController
@RequestMapping(value = "/qwen")
@RequiredArgsConstructor
public class QwenController {
    private final ChatQwen chatQwen;

    @ApiOperation(value = "大模型对话接口-同步")
    @PostMapping(value = "/call")
    public BaseResponse<String> CallChat(@RequestBody CallQwenDto callQwenDto) {
        if (callQwenDto == null) {
            throw new RuntimeException("控制层参数为空");
        }
        String callChatResult = chatQwen.CallChat(callQwenDto);
        return ResultUtils.success(callChatResult);
    }

    @ApiOperation(value = "大模型对话接口-流式")
    @PostMapping(value = "/stream")
    public BaseResponse<Flux<String>> StreamChat(@RequestBody StreamQwenDto streamQwenDto) {
        if (streamQwenDto == null) {
            throw new RuntimeException("控制层参数为空");
        }
        Flux<String> streamChatResult = chatQwen.StreamChat(streamQwenDto);
        return ResultUtils.success(streamChatResult);
    }

}
