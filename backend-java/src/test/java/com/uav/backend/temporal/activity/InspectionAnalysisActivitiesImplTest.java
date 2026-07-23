package com.uav.backend.temporal.activity;

import com.uav.backend.ai.client.AiChatClient;
import com.uav.backend.ai.service.InspectionAnalysisPromptService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InspectionAnalysisActivitiesImplTest {

    @Test
    void shouldSendPromptAndOriginalQuestionToAi() {
        AiChatClient aiChatClient = mock(AiChatClient.class);
        InspectionAnalysisPromptService promptService =
                mock(InspectionAnalysisPromptService.class);
        when(promptService.buildPrompt(
                "TASK-REAL-001",
                "是否可以继续飞行？"
        )).thenReturn("包含 MySQL 任务数据的提示词");
        when(aiChatClient.chat(
                "session-001",
                "包含 MySQL 任务数据的提示词",
                "是否可以继续飞行？"
        ))
                .thenReturn("分析结果");

        InspectionAnalysisActivitiesImpl activities =
                new InspectionAnalysisActivitiesImpl(
                        aiChatClient,
                        promptService
                );

        String result = activities.chatTask(
                "TASK-REAL-001",
                "session-001",
                "是否可以继续飞行？"
        );

        verify(aiChatClient).chat(
                "session-001",
                "包含 MySQL 任务数据的提示词",
                "是否可以继续飞行？"
        );

        assertThat(result).isEqualTo("分析结果");
    }
}
