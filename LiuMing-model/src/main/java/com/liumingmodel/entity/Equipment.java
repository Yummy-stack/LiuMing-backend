package com.liumingmodel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category; // 人员、装备、舰艇、战备工程

    private String mainInfo; // 主要信息 (简介字段)

    private Double length;

    private Double size;

    private String filePath; // MinIO中的文件路径

    private String sourceUrl; // 原始HTML页面地址
}
