package com.liumingservices.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liumingservices.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
    List<Equipment> selectAllEquipments();

    List<Equipment> selectListByLastId(
            @Param(value = "lastId") Long lastId,
            @Param(value = "pageSize") Integer pageSize
    );
}
