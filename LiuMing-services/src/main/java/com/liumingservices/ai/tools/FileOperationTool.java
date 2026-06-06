package com.liumingservices.ai.tools;

import cn.hutool.core.io.FileUtil;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import static com.liumingcommon.constants.file.FileConstants.FIR_SAVE_DIR;

public class FileOperationTool {
    @Tool(description = "Read content from a file")
    public String readFile(
            @ToolParam(description = "Name of the file to read") String fileName
    ) {
        try {
            String filePath = FIR_SAVE_DIR + "/" + fileName;
            if (StrUtil.isBlank(fileName)) {
                throw new RuntimeException("fileName is empty");
            }
            return FileUtil.readUtf8String(filePath);
        } catch (RuntimeException e) {
            return "error to read file";
        }
    }

}