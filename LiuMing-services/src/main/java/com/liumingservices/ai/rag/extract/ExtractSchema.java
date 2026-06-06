package com.liumingservices.ai.rag.extract;

import com.liumingmodel.entity.Equipment;
import com.liumingservices.ai.rag.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExtractSchema {

    private final EquipmentMapper equipmentMapper;

    /**
     * 从MySQL数据库中提取装备的简介信息
     */
    public List<Document> extractFromDatabase() {
        log.info("开始从数据库提取数据...");
        List<Equipment> equipmentList = equipmentMapper.selectList(null);
        
        return equipmentList.stream().map(equipment -> {
            String content = String.format("名称: %s\n类型: %s\n主要信息: %s\n长度: %s\n大小: %s",
                    equipment.getName(),
                    equipment.getCategory(),
                    equipment.getMainInfo(),
                    equipment.getLength(),
                    equipment.getSize());
            
            Document document = new Document(content);
            document.getMetadata().put("id", equipment.getId());
            document.getMetadata().put("source", "mysql");
            document.getMetadata().put("name", equipment.getName());
            return document;
        }).collect(Collectors.toList());
    }
}

