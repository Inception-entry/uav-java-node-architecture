package com.uav.backend.temporal;

import com.uav.backend.ai.dto.InspectionAnalysisRequest;
import com.uav.backend.ai.dto.InspectionAnalysisResponse;
import com.uav.backend.common.ApiResponse;
import com.uav.backend.temporal.workflow.InspectionAnalysisWorkflow;
import com.uav.backend.temporal.workflow.InspectionWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inspection-workflows")
public class InspectionWorkflowController {

    private static final String TASK_QUEUE = "uav-inspection-task-queue";

    private final WorkflowClient workflowClient;

    public InspectionWorkflowController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/{taskCode}")
    public ApiResponse<Map<String, String>> start(
            @PathVariable String taskCode) {
        InspectionWorkflow workflow = workflowClient.newWorkflowStub(
                InspectionWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("inspection-" + taskCode)
                        .setTaskQueue(TASK_QUEUE)
                        .build()
        );

        WorkflowExecution execution =
                WorkflowClient.start(workflow::start, taskCode);

        return ApiResponse.ok(Map.of(
                "workflowId", execution.getWorkflowId(),
                "runId", execution.getRunId()
        ));
    }

    @GetMapping("/{taskCode}/status")
    public ApiResponse<Map<String, String>> status(
            @PathVariable String taskCode) {
        String status = getWorkflow(taskCode).getStatus();

        return ApiResponse.ok(Map.of(
                "taskCode", taskCode,
                "status", status
        ));
    }

    @PostMapping("/{taskCode}/complete")
    public ApiResponse<Void> complete(@PathVariable String taskCode) {
        getWorkflow(taskCode).complete();
        return ApiResponse.ok(null);
    }

    @PostMapping("/{taskCode}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String taskCode) {
        getWorkflow(taskCode).cancel();
        return ApiResponse.ok(null);
    }

    @PostMapping("/{taskCode}/analysis")
    public ApiResponse<InspectionAnalysisResponse> analyze(
            @PathVariable String taskCode,
            @Valid @RequestBody InspectionAnalysisRequest request) {
        String workflowId = "inspection-analysis-"
                + taskCode + "-" + UUID.randomUUID();

        InspectionAnalysisWorkflow workflow =
                workflowClient.newWorkflowStub(
                        InspectionAnalysisWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(workflowId)
                                .setTaskQueue(TASK_QUEUE)
                                .build()
                );

        String analysis = workflow.analyze(
                taskCode,
                request.question()
        );

        return ApiResponse.ok(new InspectionAnalysisResponse(
                taskCode,
                workflowId,
                analysis
        ));
    }

    private InspectionWorkflow getWorkflow(String taskCode) {
        return workflowClient.newWorkflowStub(
                InspectionWorkflow.class,
                "inspection-" + taskCode
        );
    }
}
