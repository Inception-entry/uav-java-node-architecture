package com.uav.backend.temporal;

import com.uav.backend.common.ApiResponse;
import com.uav.backend.temporal.workflow.HelloWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/temporal")
public class TemporalDemoController {
    private static final String TASK_QUEUE =
            "uav-inspection-task-queue";

    private final WorkflowClient workflowClient;

    public TemporalDemoController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/demo")
    public ApiResponse<Map<String, String>> start(
            @RequestParam(defaultValue = "UAV") String name) {

        HelloWorkflow workflow = workflowClient.newWorkflowStub(
                HelloWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("hello-" + UUID.randomUUID())
                        .build()
        );

        WorkflowExecution execution =
                WorkflowClient.start(workflow::run, name);

        return ApiResponse.ok(Map.of(
                "workflowId", execution.getWorkflowId(),
                "runId", execution.getRunId()
        ));
    }
}
