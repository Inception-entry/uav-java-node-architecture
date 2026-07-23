package com.uav.backend.ai.dto;

import com.uav.backend.ai.domain.AnalysisChannel;

import java.time.LocalDateTime;
import java.util.List;

public record InspectionAnalysisRecordResponse(
        String analysisId,
        String taskCode,
        String sessionId,
        AnalysisChannel channel,
        String question,
        String answer,
        String model,
        List<AiKnowledgeSource> sources,
        LocalDateTime createdAt
) {
}
