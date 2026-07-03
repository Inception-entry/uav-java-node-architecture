package com.uav.backend.task.service;

import com.uav.backend.task.domain.InspectionTask;
import com.uav.backend.task.dto.InspectionTaskResponse;
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
        InspectionTask task = repository
                .findByTaskCode(taskCode)
                .orElseThrow(() -> new NoSuchElementException(
                        "巡检任务不存在：" + taskCode
                ));

        return toResponse(task);
    }

    private InspectionTaskResponse toResponse(
            InspectionTask task) {
        return new InspectionTaskResponse(
                task.getTaskCode(),
                task.getTaskName(),
                task.getStatus()
        );
    }
}