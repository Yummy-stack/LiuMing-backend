package com.liumingservices.entity;

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
    @ApiModelProperty(value = "主键ID（雪花算法生成）")
    private Long id;

    @ApiModelProperty(value = "装备名称")
    private String name;

    @ApiModelProperty(value = "类别（人员、装备、舰艇、战备工程）")
    private String category;

    @ApiModelProperty(value = "主要信息/简介")
    private String mainInfo;

    @ApiModelProperty(value = "长度（单位：米）")
    private Double length;

    @ApiModelProperty(value = "尺寸/大小（单位：平方米或立方米，视业务而定）")
    private Double size;

    @ApiModelProperty(value = "MinIO中的文件路径")
    private String filePath;

    @ApiModelProperty(value = "原始HTML页面地址")
    private String sourceUrl;
}
