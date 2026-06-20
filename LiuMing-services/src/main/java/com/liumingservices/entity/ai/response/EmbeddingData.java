package com.liumingservices.entity.ai.response;

import lombok.Data;

import java.util.List;

@Data
public class EmbeddingData {
    private Integer index;

    private List<Float> embedding;

    private String type;
}
