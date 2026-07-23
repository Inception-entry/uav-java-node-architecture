package com.uav.backend.temporal.activity;

import com.uav.backend.ai.client.AiChatClient;
import com.uav.backend.ai.service.InspectionAnalysisPromptService;
import io.temporal.activity.Activity;
import org.springframework.stereotype.Component;

@Component("inspectionAnalysisActivities")
public class InspectionAnalysisActivitiesImpl
        implements InspectionAnalysisActivities {

    private final AiChatClient aiChatClient;
    private final InspectionAnalysisPromptService promptService;

    public InspectionAnalysisActivitiesImpl(
            AiChatClient aiChatClient,
            InspectionAnalysisPromptService promptService) {
        this.aiChatClient = aiChatClient;
        this.promptService = promptService;
    }

    @Override
    public String analyzeTask(String taskCode, String question) {
        String sessionId = Activity.getExecutionContext()
                .getInfo()
                .getWorkflowId();
        return chatTask(taskCode, sessionId, question);
    }

    @Override
    public String chatTask(
            String taskCode,
            String sessionId,
            String question) {
        String prompt = promptService.buildPrompt(taskCode, question);

        return aiChatClient.chat(sessionId, prompt, question);
    }
}
