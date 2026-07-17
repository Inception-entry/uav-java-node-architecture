package com.uav.backend.task.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_task")
public class InspectionTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", nullable = false, unique = true, length = 64)
    private String taskCode;

    @Column(name = "task_name", nullable = false, length = 128)
    private String taskName;

    @Column(name = "device_code", length = 64)
    private String deviceCode;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "plan_start_time")
    private LocalDateTime planStartTime;

    @Column(name = "plan_end_time")
    private LocalDateTime planEndTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected InspectionTask() {
    }

    public InspectionTask(String taskCode) {
        this(
                taskCode,
                "巡检任务-" + taskCode,
                null,
                null,
                null
        );
    }

    public InspectionTask(
            String taskCode,
            String taskName,
            String deviceCode,
            LocalDateTime planStartTime,
            LocalDateTime planEndTime) {
        this.taskCode = taskCode;
        this.taskName = taskName;
        this.deviceCode = deviceCode;
        this.status = "CREATED";
        this.planStartTime = planStartTime;
        this.planEndTime = planEndTime;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDetails(
            String taskName,
            String deviceCode,
            LocalDateTime planStartTime,
            LocalDateTime planEndTime) {
        this.taskName = taskName;
        this.deviceCode = deviceCode;
        this.planStartTime = planStartTime;
        this.planEndTime = planEndTime;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isTerminal() {
        return "COMPLETED".equals(status)
                || "CANCELLED".equals(status);
    }

    public void changeStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTaskCode() {
        return taskCode;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getPlanStartTime() {
        return planStartTime;
    }

    public LocalDateTime getPlanEndTime() {
        return planEndTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
