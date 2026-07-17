package com.uav.backend.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateInspectionTaskRequest(
        @NotBlank(message = "任务名称不能为空")
        @Size(max = 128, message = "任务名称不能超过 128 个字符")
        String taskName,

        @NotBlank(message = "设备编号不能为空")
        @Size(max = 64, message = "设备编号不能超过 64 个字符")
        String deviceCode,

        @NotNull(message = "计划开始时间不能为空")
        LocalDateTime planStartTime,

        @NotNull(message = "计划结束时间不能为空")
        LocalDateTime planEndTime
) {
}
