package com.liumingservices.ai.tools.config;

import com.liumingservices.ai.tools.FileOperationTool;
import com.liumingservices.ai.tools.test.TestTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ToolRegistration {
    private final FileOperationTool fileOperationTool;

    private final TestTool testTool;

    @Bean
    public ToolCallback[] allTools() {
        return ToolCallbacks.from(
                fileOperationTool,
                testTool
        );
    }
}
