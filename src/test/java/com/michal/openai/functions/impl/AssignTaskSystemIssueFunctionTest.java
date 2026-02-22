package com.michal.openai.functions.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.michal.openai.tasksystem.entity.dto.TaskSystemCommentDto;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.service.TaskSystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AssignTaskSystemIssueFunctionTest {

    private TaskSystemService taskSystemService;
    private AssignTaskSystemIssueFunction function;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        taskSystemService = mock(TaskSystemService.class);
        function = new AssignTaskSystemIssueFunction(taskSystemService, objectMapper);
    }

    @Test
    void shouldReturnJsonFromDto() throws Exception {
        String requestBody = "{\"key\":\"Dummy-1\"}";
        TaskSystemIssueDto dto = getDto();
        when(taskSystemService.assignIssue(requestBody)).thenReturn(dto);

        String result = function.execute(requestBody);

        assertThat(result).contains("\"key\":\"Dummy-1\"");
        verify(taskSystemService).assignIssue(requestBody);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenServiceFails() throws Exception {
        String requestBody = "{\"key\":\"Dummy-1\"}";
        when(taskSystemService.assignIssue(requestBody)).thenThrow(new RuntimeException("fail"));

        assertThatThrownBy(() -> function.execute(requestBody))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("fail");
    }

    @Test
    void shouldThrowRuntimeExceptionWhenJsonFails() throws Exception {
        String requestBody = "{\"key\":\"Dummy-1\"}";
        TaskSystemIssueDto dto = mock(TaskSystemIssueDto.class);
        when(taskSystemService.assignIssue(requestBody)).thenReturn(dto);

    }

    private TaskSystemIssueDto getDto() {
        List<Integer> attachmentIds = new ArrayList<>();
        attachmentIds.add(1);
        var commentDto = new TaskSystemCommentDto(1, 1, "conent", 1, OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"), "authorName",
                attachmentIds,
                "slackUserId");
        List<TaskSystemCommentDto> comments = new ArrayList<>();
        comments.add(commentDto);
        return new TaskSystemIssueDto(1, "Dummy-1", "Title", "Desc", "NEW", "HIGH", "U12345678", "U12345677",
                OffsetDateTime.parse("2025-09-15T19:32:24Z") , OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"),
                comments,
                1
        );
    }
}
