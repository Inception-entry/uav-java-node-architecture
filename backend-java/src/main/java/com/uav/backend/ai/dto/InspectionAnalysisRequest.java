package com.uav.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InspectionAnalysisRequest(
        @NotBlank(message = "分析问题不能为空")
        @Size(max = 2000, message = "分析问题不能超过 2000 个字符")
        String question
) {
}
