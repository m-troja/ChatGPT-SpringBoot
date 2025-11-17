package com.michal.openai.functions.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;
import com.michal.openai.tasksystem.service.TaskSystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateTaskSystemIssueFunctionTest {

    private TaskSystemService taskSystemService;
    private ObjectMapper objectMapper;
    private CreateTaskSystemIssueFunction function;

    @BeforeEach
    public void setup() {
        taskSystemService = mock(TaskSystemService.class);
        objectMapper = new ObjectMapper();
        function = new CreateTaskSystemIssueFunction(taskSystemService, objectMapper);
    }

    @Test
    public void shouldCreateTaskSystemIssue() throws Exception {
        // given
        String requestBody = buildRequestBody();
        when(taskSystemService.createIssue(requestBody)).thenReturn(buildTaskSystemIssueDto());

        // when
        String functionResult = function.execute(requestBody);

        // then
        assertThat(functionResult).isEqualTo(objectMapper.writeValueAsString(buildTaskSystemIssueDto()));
    }

    private String buildRequestBody() {
        return """
                {\\"title\\":\\"Set duedate parameter as optional\\",\\"description\\":\\"As a user, I want the 'duedate' parameter in the task-system to be optional, so that creating or updating tickets does not always require specifying a due date.\\",\\"priority\\":\\"NORMAL\\",\\"authorSlackId\\":\\"U08RQ4PPVNW\\",\\"assigneeSlackId\\":\\"U08SHTW059C\\"}
                """;
    }

    private TaskSystemIssueDto buildTaskSystemIssueDto() {
        return new TaskSystemIssueDto("JAVA-1", "Set duedate parameter as optional", "As a user, I want the 'duedate' parameter in the task-system to be optional, so that creating or updating tickets does not always require specifying a due date.", "NEW", "NORMAL", "U08RQ4PPVNW", "U08SHTW059C");
    }
}
