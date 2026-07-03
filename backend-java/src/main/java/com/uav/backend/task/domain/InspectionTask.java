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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected InspectionTask() {
    }

    public InspectionTask(String taskCode) {
        this.taskCode = taskCode;
        this.taskName = "巡检任务-" + taskCode;
        this.status = "CREATED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public String getStatus() {
        return status;
    }
}