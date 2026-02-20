package com.michal.openai.functions.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.michal.openai.tasksystem.entity.dto.TaskSystemCommentDto;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.service.TaskSystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateTaskSystemIssueFunctionTest {

    private TaskSystemService taskSystemService;
    @Autowired ObjectMapper objectMapper;
    private CreateTaskSystemIssueFunction function;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        taskSystemService = mock(TaskSystemService.class);
        function = new CreateTaskSystemIssueFunction(taskSystemService, objectMapper);
    }

    @Test
    public void shouldCreateTaskSystemIssue() throws Exception {
        // given
        String requestBody = buildRequestBody();
        when(taskSystemService.createIssue(requestBody)).thenReturn(getDto());

        // when
        String functionResult = function.execute(requestBody);

        // then
        assertThat(functionResult).isEqualTo(objectMapper.writeValueAsString(getDto()));
    }

    private String buildRequestBody() {
        return """
                {\\"title\\":\\"Set duedate parameter as optional\\",\\"description\\":\\"As a user, I want the 'duedate' parameter in the task-system to be optional, so that creating or updating tickets does not always require specifying a due date.\\",\\"priority\\":\\"NORMAL\\",\\"authorSlackId\\":\\"U08RQ4PPVNW\\",\\"assigneeSlackId\\":\\"U08SHTW059C\\"}
                """;
    }

    private TaskSystemIssueDto getDto() {
        var commentDto = new TaskSystemCommentDto(1, 1, "conent", 1, OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"), "authorName");
        List<TaskSystemCommentDto> comments = new ArrayList<>();
        comments.add(commentDto);
        return new TaskSystemIssueDto(1, "Dummy-1", "Title", "Desc", "NEW", "HIGH", "U12345678", "U12345677",
                OffsetDateTime.parse("2025-09-15T19:32:24Z") , OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"),
                comments,
                1
        );
    }
}
