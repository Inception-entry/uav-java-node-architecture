package com.uav.backend.temporal.activity;

import com.uav.backend.ai.client.AiChatClient;
import com.uav.backend.task.dto.InspectionTaskAnalysisContext;
import com.uav.backend.task.service.InspectionTaskService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InspectionAnalysisActivitiesImplTest {

    @Test
    void shouldIncludeMysqlTaskContextInAiPrompt() {
        AiChatClient aiChatClient = mock(AiChatClient.class);
        InspectionTaskService taskService =
                mock(InspectionTaskService.class);
        LocalDateTime planStart =
                LocalDateTime.of(2026, 7, 17, 9, 30);
        LocalDateTime planEnd =
                LocalDateTime.of(2026, 7, 17, 11, 30);

        when(taskService.findAnalysisContext("TASK-REAL-001"))
                .thenReturn(new InspectionTaskAnalysisContext(
                        "TASK-REAL-001",
                        "东区输电线路巡检",
                        "UAV-001",
                        "RUNNING",
                        planStart,
                        planEnd,
                        LocalDateTime.of(2026, 7, 16, 18, 0),
                        LocalDateTime.of(2026, 7, 17, 9, 35)
                ));
        when(aiChatClient.chat(
                org.mockito.ArgumentMatchers.eq("session-001"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq("是否可以继续飞行？")
        ))
                .thenReturn("分析结果");

        InspectionAnalysisActivitiesImpl activities =
                new InspectionAnalysisActivitiesImpl(
                        aiChatClient,
                        taskService
                );

        String result = activities.chatTask(
                "TASK-REAL-001",
                "session-001",
                "是否可以继续飞行？"
        );

        ArgumentCaptor<String> prompt =
                ArgumentCaptor.forClass(String.class);
        verify(aiChatClient).chat(
                org.mockito.ArgumentMatchers.eq("session-001"),
                prompt.capture(),
                org.mockito.ArgumentMatchers.eq("是否可以继续飞行？")
        );

        assertThat(result).isEqualTo("分析结果");
        assertThat(prompt.getValue())
                .contains("东区输电线路巡检")
                .contains("UAV-001")
                .contains("RUNNING")
                .contains("2026-07-17T09:30")
                .contains("是否可以继续飞行？");
    }
}
