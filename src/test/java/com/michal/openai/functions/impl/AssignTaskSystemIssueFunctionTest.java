package com.michal.openai.functions.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.service.TaskSystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AssignTaskSystemIssueFunctionTest {

    private TaskSystemService taskSystemService;
    private AssignTaskSystemIssueFunction function;

    @BeforeEach
    void setup() {
        taskSystemService = mock(TaskSystemService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        function = new AssignTaskSystemIssueFunction(taskSystemService, objectMapper);
    }

    @Test
    void shouldReturnJsonFromDto() throws Exception {
        String requestBody = "{\"key\":\"Dummy-1\"}";
        TaskSystemIssueDto dto = new TaskSystemIssueDto("Dummy-1", "Title", "Desc", "NEW", "HIGH", "U12345678", "U12345677");
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
}
