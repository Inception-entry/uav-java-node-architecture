package com.uav.backend.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface InspectionAnalysisActivities {

    @ActivityMethod
    String analyzeTask(String taskCode, String question);
}
