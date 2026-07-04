package com.uav.backend.temporal.activity;

import com.uav.backend.ai.client.AiChatClient;
import org.springframework.stereotype.Component;

@Component("inspectionAnalysisActivities")
public class InspectionAnalysisActivitiesImpl
        implements InspectionAnalysisActivities {

    private final AiChatClient aiChatClient;

    public InspectionAnalysisActivitiesImpl(AiChatClient aiChatClient) {
        this.aiChatClient = aiChatClient;
    }

    @Override
    public String analyzeTask(String taskCode, String question) {
        String prompt = """
                请分析下面的无人机巡检任务，并给出明确、可执行的建议。

                任务编号：%s
                分析问题：%s

                请重点考虑飞行安全、通信时延、故障恢复和操作优先级。
                """.formatted(taskCode, question);

        return aiChatClient.chat(prompt);
    }
}
