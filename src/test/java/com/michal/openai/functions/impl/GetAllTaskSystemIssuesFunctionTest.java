package com.michal.openai.functions.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
        objectMapper.registerModule(new JavaTimeModule());
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
        List<Integer> attachmentIds = new ArrayList<>();
        attachmentIds.add(1);
        var commentDto1 = new TaskSystemCommentDto(1, 1, "conent", 1, OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"), "authorName", attachmentIds);
        List<TaskSystemCommentDto> comments1 = new ArrayList<>();
        comments1.add(commentDto1);
        var issue1=  new TaskSystemIssueDto(1, "Dummy-1", "Title", "Desc", "NEW", "HIGH", "U12345678", "U12345677",
                OffsetDateTime.parse("2025-09-15T19:32:24Z") , OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"),
                comments1,
                1
        );
        var commentDto2 = new TaskSystemCommentDto(1, 1, "conent", 1, OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"), "authorName", attachmentIds);
        List<TaskSystemCommentDto> comments2 = new ArrayList<>();
        comments2.add(commentDto2);
        var issue2=  new TaskSystemIssueDto(1, "Dummy-1", "Title", "Desc", "NEW", "HIGH", "U12345678", "U12345677",
                OffsetDateTime.parse("2025-09-15T19:32:24Z") , OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"),
                comments1,
                1
        );

        return List.of(issue1, issue2);
    }
}
