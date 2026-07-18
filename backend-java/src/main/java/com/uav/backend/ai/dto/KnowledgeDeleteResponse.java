package com.uav.backend.ai.dto;

public record KnowledgeDeleteResponse(
        String documentId,
        int deletedChunks) {
}
