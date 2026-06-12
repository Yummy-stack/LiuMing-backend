package com.liumingservices.ai.tools.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestTool {

    @Tool(description = "联网搜索的工具")
    public String test1(
            @ToolParam(description = "搜索的内容") String searchContent
    ) {
        return "the content";
    }
}
