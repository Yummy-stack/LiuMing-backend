package com.liumingservices.demon;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Api(tags = "TestApi")
@RestController
@RequestMapping(value = "/ai")
public class DemonAi {
    @Resource
    private ChatModel dashscopeChatModel;

    @ApiOperation(value = "测试-1")
    @PostMapping(value = "/test1", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> test1(
            @ApiParam(value = "mvc请求体") HttpServletRequest httpServletRequest,
            @ApiParam(value = "mvc响应体") HttpServletResponse httpServletResponse
    ) {
        ChatClient chatClient1 = ChatClient.builder(dashscopeChatModel)
                .defaultSystem("你是恋爱固问")
                .build();

        Flux<String> stringFlux = chatClient1.prompt()
                .user("")
                .stream()
                .content();

        return stringFlux.flatMap(Flux::just);
    }
}
