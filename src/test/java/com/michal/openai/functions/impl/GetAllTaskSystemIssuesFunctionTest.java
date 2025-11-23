package com.michal.openai.functions.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.service.TaskSystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAllTaskSystemIssuesFunctionTest {

    private TaskSystemService taskSystemService;
    private ObjectMapper objectMapper;
    private GetAllTaskSystemIssuesFunction function;

    @BeforeEach
    public void init() {
        taskSystemService = mock(TaskSystemService.class);
        objectMapper = new ObjectMapper();
        function = new GetAllTaskSystemIssuesFunction(taskSystemService, objectMapper);
    }

    @Test
    public void shouldGetAllTaskSystemIssues() throws JsonProcessingException {
        //given
        var issuesMock = buildTaskSystemIssueDto();
        when(taskSystemService.getAllIssues()).thenReturn(issuesMock);

        // when
        String issuesFromService = function.execute(null);

        // then
        var issuesParsedFromService = objectMapper.readValue(issuesFromService, new TypeReference<List<TaskSystemIssueDto>>() {});

        assertThat(issuesParsedFromService).isEqualTo(issuesMock);
    }

    private List<TaskSystemIssueDto> buildTaskSystemIssueDto() {
        var issue1 = new TaskSystemIssueDto("JAVA-1", "Set duedate parameter as optional", "As a user, I want the 'duedate' parameter in the task-system to be optional, so that creating or updating tickets does not always require specifying a due date.", "NEW", "NORMAL", "U08RQ4PPVNW", "U08SHTW059C");
        var issue2 = new TaskSystemIssueDto("JAVA-2", "Set duedate parameter as optional", "As a user, I want the 'duedate' parameter in the task-system to be optional, so that creating or updating tickets does not always require specifying a due date.", "NEW", "NORMAL", "U08RQ4PPVNW", "U08SHTW059C");
        return List.of(issue1, issue2);
    }
}
