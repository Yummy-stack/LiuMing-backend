package com.liumingservices.ai.love.tools.config;

import com.liumingservices.ai.love.tools.FileOperationTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ToolRegistration {
    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();

        return ToolCallbacks.from(
                fileOperationTool
        );
    }
}
