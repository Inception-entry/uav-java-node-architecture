package com.uav.backend.ai.dto;

public record AiChatRequest(
        String sessionId,
        String message,
        String knowledgeQuery) {
}
