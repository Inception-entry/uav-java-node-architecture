package com.uav.backend.task;

import com.uav.backend.common.ApiResponse;
import com.uav.backend.task.dto.InspectionTaskResponse;
import com.uav.backend.task.service.InspectionTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/inspection-tasks")
public class InspectionTaskController {
    private final InspectionTaskService inspectionTaskService;

    public InspectionTaskController(InspectionTaskService inspectionTaskService) {
        this.inspectionTaskService = inspectionTaskService;
    }

    @GetMapping
    public ApiResponse<List<InspectionTaskResponse>> list() {
        return ApiResponse.ok(inspectionTaskService.findAll());
    }

    @GetMapping("/{taskCode}")
    public ApiResponse<InspectionTaskResponse> detail(@PathVariable String taskCode) {
        return ApiResponse.ok(inspectionTaskService.findByTaskCode(taskCode));
    }
}
