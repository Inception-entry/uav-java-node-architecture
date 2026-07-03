package com.uav.backend.task.dto;

public record InspectionTaskResponse (
  String taskCode,
  String taskName,
  String status
) {
}
