package com.liumingservices.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.date.DateUtil;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.util.Date;

import static com.liumingcommon.constants.file.FileConstants.FIR_SAVE_DIR;

@Component
public class FileOperationTool {

    @Tool(description = "Read content from a file")
    public String readFile(
            @ToolParam(description = "Name of the file to read") String fileName) {
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

    @Tool(description = "Export content to a Word document (.doc)")
    public String exportToDoc(
            @ToolParam(description = "Title of the document") String title,
            @ToolParam(description = "Main content of the document") String content,
            @ToolParam(description = "File name (optional, will be generated if empty)") String fileName) {
        try {
            String name = StrUtil.isBlank(fileName)
                    ? "document_" + DateUtil.format(new Date(), "yyyy-MM-dd-HH:mm:ss") + ".docx"
                    : fileName;
            if (!name.endsWith(".docx")) {
                name += ".docx";
            }
            String filePath = FIR_SAVE_DIR + "/" + name;

            XWPFDocument document = new XWPFDocument();

            // 添加标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(title);
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setFontFamily("宋体");

            // 添加空行
            document.createParagraph();

            // 添加内容
            XWPFParagraph contentParagraph = document.createParagraph();
            XWPFRun contentRun = contentParagraph.createRun();
            contentRun.setText(content);
            contentRun.setFontSize(12);
            contentRun.setFontFamily("宋体");

            // 保存文档
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }

            return "Word document exported successfully: " + filePath;
        } catch (Exception e) {
            return "Error exporting Word document: " + e.getMessage();
        }
    }

    @Tool(description = "Export content to a PDF document")
    public String exportToPdf(
            @ToolParam(description = "Title of the document") String title,
            @ToolParam(description = "Main content of the document") String content,
            @ToolParam(description = "File name (optional, will be generated if empty)") String fileName
    ) {
        try {
            String name = StrUtil.isBlank(fileName)
                    ? "document_" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".pdf"
                    : fileName;
            if (!name.endsWith(".pdf")) {
                name += ".pdf";
            }
            String filePath = FIR_SAVE_DIR + "/" + name;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // 添加标题
            Font titleFont = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", 18, Font.BOLD);
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(titleParagraph);

            // 添加空行
            document.add(new Paragraph(" "));

            // 添加内容
            Font contentFont = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", 12);
            Paragraph contentParagraph = new Paragraph(content, contentFont);
            document.add(contentParagraph);

            document.close();

            return "PDF document exported successfully: " + filePath;
        } catch (Exception e) {
            return "Error exporting PDF document: " + e.getMessage();
        }
    }

}