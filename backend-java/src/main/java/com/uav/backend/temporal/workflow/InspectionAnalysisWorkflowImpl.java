package com.uav.backend.temporal.workflow;

import com.uav.backend.temporal.activity.InspectionAnalysisActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class InspectionAnalysisWorkflowImpl
        implements InspectionAnalysisWorkflow {

    private final InspectionAnalysisActivities activities =
            Workflow.newActivityStub(
                    InspectionAnalysisActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(
                                    Duration.ofSeconds(150))
                            .setRetryOptions(
                                    RetryOptions.newBuilder()
                                            .setInitialInterval(
                                                    Duration.ofSeconds(2))
                                            .setMaximumAttempts(3)
                                            .build()
                            )
                            .build()
            );

    @Override
    public String analyze(String taskCode, String question) {
        return activities.analyzeTask(taskCode, question);
    }
}
