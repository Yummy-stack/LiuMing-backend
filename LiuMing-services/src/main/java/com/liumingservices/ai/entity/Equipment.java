package com.liumingservices.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName(value = "equipment")
@ApiModel(description = "装备信息")
public class Equipment {
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID（雪花算法生成）", example = "1683123456789012345")
    private Long id;

    @ApiModelProperty(value = "装备名称", example = "歼-20战斗机")
    private String name;

    @ApiModelProperty(value = "类别（人员、装备、舰艇、战备工程）", example = "装备")
    private String category;

    @ApiModelProperty(value = "主要信息/简介", example = "第五代双发重型隐身战斗机")
    private String mainInfo;

    @ApiModelProperty(value = "长度（单位：米）", example = "21.2")
    private Double length;

    @ApiModelProperty(value = "尺寸/大小（单位：平方米或立方米，视业务而定）", example = "73.0")
    private Double size;

    @ApiModelProperty(value = "MinIO中的文件路径", example = "/equipment/2026/j20.jpg")
    private String filePath;

    @ApiModelProperty(value = "原始HTML页面地址", example = "https://www.example.com/detail?id=123")
    private String sourceUrl;
}
