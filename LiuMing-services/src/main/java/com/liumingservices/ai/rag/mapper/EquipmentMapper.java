package com.liumingservices.ai.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liumingservices.ai.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
    List<Equipment> selectAllEquipments();
}
