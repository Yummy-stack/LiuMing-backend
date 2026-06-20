package com.liumingservices.entity.ai.response;

import lombok.Data;

import java.util.List;

@Data
public class Output {
    private List<EmbeddingData> embeddings;
}
