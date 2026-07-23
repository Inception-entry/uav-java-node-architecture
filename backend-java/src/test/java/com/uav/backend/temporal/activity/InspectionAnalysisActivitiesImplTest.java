package com.uav.backend.temporal.activity;

import com.uav.backend.ai.client.AiChatClient;
import com.uav.backend.ai.domain.AnalysisChannel;
import com.uav.backend.ai.dto.AiChatResponse;
import com.uav.backend.ai.dto.InspectionAnalysisRecordResponse;
import com.uav.backend.ai.service.InspectionAnalysisPromptService;
import com.uav.backend.ai.service.InspectionAnalysisRecordService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InspectionAnalysisActivitiesImplTest {

    @Test
    void shouldSendPromptAndOriginalQuestionToAi() {
        AiChatClient aiChatClient = mock(AiChatClient.class);
        InspectionAnalysisPromptService promptService =
                mock(InspectionAnalysisPromptService.class);
        InspectionAnalysisRecordService recordService =
                mock(InspectionAnalysisRecordService.class);
        when(recordService.findCompletedAnswer("analysis-001"))
                .thenReturn(Optional.empty());
        when(promptService.buildPrompt(
                "TASK-REAL-001",
                "是否可以继续飞行？"
        )).thenReturn("包含 MySQL 任务数据的提示词");
        when(aiChatClient.chatResponse(
                "session-001",
                "包含 MySQL 任务数据的提示词",
                "是否可以继续飞行？"
        ))
                .thenReturn(new AiChatResponse(
                        "my-drone-expert",
                        "分析结果",
                        List.of()
                ));
        when(recordService.saveCompleted(
                "analysis-001",
                "TASK-REAL-001",
                "session-001",
                AnalysisChannel.TEMPORAL,
                "是否可以继续飞行？",
                "分析结果",
                "my-drone-expert",
                List.of()
        )).thenReturn(new InspectionAnalysisRecordResponse(
                "analysis-001",
                "TASK-REAL-001",
                "session-001",
                AnalysisChannel.TEMPORAL,
                "是否可以继续飞行？",
                "分析结果",
                "my-drone-expert",
                List.of(),
                LocalDateTime.now()
        ));

        InspectionAnalysisActivitiesImpl activities =
                new InspectionAnalysisActivitiesImpl(
                        aiChatClient,
                        promptService,
                        recordService
                );

        String result = activities.chatTask(
                "TASK-REAL-001",
                "session-001",
                "是否可以继续飞行？",
                "analysis-001"
        );

        verify(aiChatClient).chatResponse(
                "session-001",
                "包含 MySQL 任务数据的提示词",
                "是否可以继续飞行？"
        );
        verify(recordService).saveCompleted(
                "analysis-001",
                "TASK-REAL-001",
                "session-001",
                AnalysisChannel.TEMPORAL,
                "是否可以继续飞行？",
                "分析结果",
                "my-drone-expert",
                List.of()
        );

        assertThat(result).isEqualTo("分析结果");
    }

    @Test
    void shouldReusePersistedAnswerWhenTemporalRetries() {
        AiChatClient aiChatClient = mock(AiChatClient.class);
        InspectionAnalysisPromptService promptService =
                mock(InspectionAnalysisPromptService.class);
        InspectionAnalysisRecordService recordService =
                mock(InspectionAnalysisRecordService.class);
        when(recordService.findCompletedAnswer("analysis-retry"))
                .thenReturn(Optional.of("已保存的答案"));

        InspectionAnalysisActivitiesImpl activities =
                new InspectionAnalysisActivitiesImpl(
                        aiChatClient,
                        promptService,
                        recordService
                );

        String result = activities.chatTask(
                "TASK-REAL-001",
                "session-001",
                "是否可以继续飞行？",
                "analysis-retry"
        );

        assertThat(result).isEqualTo("已保存的答案");
        verifyNoInteractions(aiChatClient, promptService);
        verify(recordService, never()).saveCompleted(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyList()
        );
    }
}
