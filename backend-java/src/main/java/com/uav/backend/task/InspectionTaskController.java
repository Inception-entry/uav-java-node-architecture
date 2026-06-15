package com.uav.backend.task;

import com.uav.backend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inspection-tasks")
public class InspectionTaskController {
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(List.of(
                Map.of("taskCode", "TASK-001", "taskName", "园区人员持械巡检", "status", "RUNNING")
        ));
    }
}
