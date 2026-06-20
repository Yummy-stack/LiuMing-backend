package com.liumingservices.ai.rag.service.ai;

import com.liumingservices.ai.rag.extract.ExtractHTML;
import com.liumingservices.ai.rag.extract.ExtractMinIO;
import com.liumingservices.ai.rag.extract.ExtractSchema;
import com.liumingservices.ai.rag.load.LoadElasticsearch;
import com.liumingservices.ai.rag.load.LoadQdrant;
import com.liumingservices.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ETLService {

    private final ExtractSchema extractSchema;

    private final ExtractMinIO extractMinIO;

    private final ExtractHTML extractHTML;

    private final LoadQdrant loadQdrant;

    private final LoadElasticsearch loadElasticsearch;

    private final EquipmentMapper equipmentMapper;

    /**
     * 执行全量ETL同步
     */
    public void runETL() {
        log.info("开始执行全量ETL流程...");

        List<Document> allDocuments = new ArrayList<>();

        // 1. E: Extract
        // 从数据库提取
        List<Document> dbDocs = extractSchema.extractFromDatabase();
        allDocuments.addAll(dbDocs);

        // 从MinIO和HTML提取 (根据数据库中的记录)
//        List<Equipment> equipmentList = equipmentMapper.selectAllEquipments();
//        for (Equipment equipment : equipmentList) {
//            if (equipment.getFilePath() != null && !equipment.getFilePath().isEmpty()) {
//                // 假设存储格式为 "bucket:object"
//                String[] parts = equipment.getFilePath().split(":");
//                if (parts.length == 2) {
//                    allDocuments.addAll(extractMinIO.extractFromMinio(parts[0], parts[1]));
//                }
//            }
//            if (equipment.getSourceUrl() != null && !equipment.getSourceUrl().isEmpty()) {
//                allDocuments.addAll(extractHTML.extractFromUrl(equipment.getSourceUrl()));
//            }
//        }

        // 2. T: Transform
        // 按照段落/Token进w行分词
        TokenTextSplitter splitter = new TokenTextSplitter(500, 100, 5, 10000, true);
        List<Document> splitDocuments = splitter.apply(allDocuments);
        log.info("数据转换完成，原始文档数: {}, 分片后文档数: {}", allDocuments.size(), splitDocuments.size());

        // 3. L: Load
        // 同步到 Qdrant (向量检索)
        loadQdrant.load(splitDocuments);
        // 同步到 Elasticsearch (全文检索)
        loadElasticsearch.load(splitDocuments);

        log.info("全量ETL流程执行完毕");
    }
}
