package com.uav.backend.task.service;

import com.uav.backend.common.ConflictException;
import com.uav.backend.task.domain.InspectionTask;
import com.uav.backend.task.dto.CreateInspectionTaskRequest;
import com.uav.backend.task.dto.InspectionTaskAnalysisContext;
import com.uav.backend.task.dto.InspectionTaskResponse;
import com.uav.backend.task.dto.UpdateInspectionTaskRequest;
import com.uav.backend.task.repository.InspectionTaskRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class InspectionTaskService {

    private final InspectionTaskRepository repository;

    public InspectionTaskService(
            InspectionTaskRepository repository) {
        this.repository = repository;
    }

    public List<InspectionTaskResponse> findAll() {
        return repository.findAll(
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public InspectionTaskResponse findByTaskCode(
            String taskCode) {
        InspectionTask task = getRequiredTask(taskCode);

        return toResponse(task);
    }

    @Transactional
    public InspectionTaskResponse create(
            CreateInspectionTaskRequest request) {
        validatePlanTime(
                request.planStartTime(),
                request.planEndTime()
        );
        if (repository.existsByTaskCode(request.taskCode())) {
            throw new ConflictException(
                    "任务编号已存在：" + request.taskCode()
            );
        }

        InspectionTask task = new InspectionTask(
                request.taskCode(),
                request.taskName().trim(),
                request.deviceCode().trim(),
                request.planStartTime(),
                request.planEndTime()
        );
        return toResponse(repository.save(task));
    }

    @Transactional
    public InspectionTaskResponse update(
            String taskCode,
            UpdateInspectionTaskRequest request) {
        validatePlanTime(
                request.planStartTime(),
                request.planEndTime()
        );
        InspectionTask task = getRequiredTask(taskCode);
        if (task.isTerminal()) {
            throw new ConflictException(
                    "已完成或已取消的任务不能修改：" + taskCode
            );
        }

        task.updateDetails(
                request.taskName().trim(),
                request.deviceCode().trim(),
                request.planStartTime(),
                request.planEndTime()
        );
        return toResponse(task);
    }

    public InspectionTaskAnalysisContext findAnalysisContext(
            String taskCode) {
        InspectionTask task = getRequiredTask(taskCode);

        return new InspectionTaskAnalysisContext(
                task.getTaskCode(),
                task.getTaskName(),
                task.getDeviceCode(),
                task.getStatus(),
                task.getPlanStartTime(),
                task.getPlanEndTime(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private InspectionTask getRequiredTask(String taskCode) {
        return repository.findByTaskCode(taskCode)
                .orElseThrow(() -> new NoSuchElementException(
                        "巡检任务不存在：" + taskCode
                ));
    }

    private void validatePlanTime(
            java.time.LocalDateTime planStartTime,
            java.time.LocalDateTime planEndTime) {
        if (!planEndTime.isAfter(planStartTime)) {
            throw new IllegalArgumentException(
                    "计划结束时间必须晚于计划开始时间"
            );
        }
    }

    private InspectionTaskResponse toResponse(
            InspectionTask task) {
        return new InspectionTaskResponse(
                task.getTaskCode(),
                task.getTaskName(),
                task.getDeviceCode(),
                task.getStatus(),
                task.getPlanStartTime(),
                task.getPlanEndTime(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
