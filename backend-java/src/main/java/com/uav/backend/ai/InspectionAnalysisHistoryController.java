package com.uav.backend.ai;

import com.uav.backend.ai.dto.InspectionAnalysisRecordResponse;
import com.uav.backend.ai.service.InspectionAnalysisRecordService;
import com.uav.backend.common.ApiResponse;
import com.uav.backend.task.service.InspectionTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inspection-tasks")
public class InspectionAnalysisHistoryController {

    private final InspectionTaskService inspectionTaskService;
    private final InspectionAnalysisRecordService recordService;

    public InspectionAnalysisHistoryController(
            InspectionTaskService inspectionTaskService,
            InspectionAnalysisRecordService recordService) {
        this.inspectionTaskService = inspectionTaskService;
        this.recordService = recordService;
    }

    @GetMapping("/{taskCode}/analyses")
    public ApiResponse<List<InspectionAnalysisRecordResponse>> list(
            @PathVariable String taskCode) {
        inspectionTaskService.findByTaskCode(taskCode);
        return ApiResponse.ok(recordService.findByTaskCode(taskCode));
    }
}
