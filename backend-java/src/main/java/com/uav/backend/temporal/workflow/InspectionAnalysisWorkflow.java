package com.uav.backend.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface InspectionAnalysisWorkflow {

    @WorkflowMethod
    String analyze(String taskCode, String question);
}
