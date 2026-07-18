package com.uav.backend.ai.dto;

public record KnowledgeDocumentResponse(
        String documentId,
        String filename,
        String contentType,
        int chunkCount,
        String uploadedAt) {
}
