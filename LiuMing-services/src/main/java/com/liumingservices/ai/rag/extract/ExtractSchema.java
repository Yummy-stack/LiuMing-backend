package com.liumingservices.ai.rag.extract;

import cn.hutool.core.util.StrUtil;
import com.liumingservices.entity.Equipment;
import com.liumingservices.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExtractSchema {

    private final EquipmentMapper equipmentMapper;

    public List<Document> extractFromDatabase() {
        log.info("开始从数据库提取数据...");

        List<Equipment> equipmentList = equipmentMapper.selectAllEquipments();
        List<Document> equDocList = equipmentList.stream()
                .map(this::EquToDoc)
                .collect(Collectors.toList());

        return equDocList;
    }

    private Document EquToDoc(Equipment equipment) {
        String content = String.format("名称: %s\n类型: %s\n主要信息: %s\n长度: %s\n大小: %s",
                equipment.getName(),
                equipment.getCategory(),
                equipment.getMainInfo(),
                equipment.getLength(),
                equipment.getSize());

        Document document = new Document(content);
        Map<String, Object> documentMetadata = document.getMetadata();

        String stringId = StrUtil.toString(equipment.getId());

        documentMetadata.put("id", stringId);
        documentMetadata.put("source", "mysql");
        documentMetadata.put("name", equipment.getName());

        return document;
    }
}

