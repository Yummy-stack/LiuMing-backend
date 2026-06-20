package com.liumingmodel.vo.imagesearch;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "以图搜图检索结果")
public class ImageVectorPayloadVo {

    @ApiModelProperty(value = "词条ID", example = "1683123456789012345")
    private String id;

    @ApiModelProperty(value = "词条名称", example = "歼-20战斗机")
    private String name;

    @ApiModelProperty(value = "词条简介", example = "第五代双发重型隐身战斗机")
    private String description;

    @ApiModelProperty(value = "图片URL", example = "http://110.42.101.68:9000/LiuMing/xxx.jpg")
    private String imageUrl;

    @ApiModelProperty(value = "相似度得分", example = "0.92")
    private Float score;
}
