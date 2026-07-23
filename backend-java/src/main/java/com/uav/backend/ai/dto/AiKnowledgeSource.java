package com.uav.backend.ai.dto;

public record AiKnowledgeSource(
        String documentId,
        String filename,
        Integer page,
        double score
) {
}
