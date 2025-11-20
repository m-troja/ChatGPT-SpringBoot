package com.michal.openai.controllers;

import com.michal.openai.jira.JiraCnv;
import com.michal.openai.jira.entity.JiraIssue;
import com.michal.openai.jira.entity.JiraIssueDto;
import com.michal.openai.jira.service.JiraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JiraRestController.class)
class JiraRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private JiraCnv jiraCnv;
    @MockitoBean private JiraService service;

    @Test
    public void testGetIssue() throws Exception {
        // when
        when(service.getIssue("1")).thenReturn(buildJiraIssue());
        when(jiraCnv.convertIssueToIssueDto(buildJiraIssue())).thenReturn(buildJiraIssueDto());

        mockMvc.perform(get("/api/v1/jira/issue/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(service, times(1)).getIssue(anyString());
        verify(jiraCnv, times(1)).convertIssueToIssueDto(buildJiraIssue());
        var issueFromService = service.getIssue("1");
        assertThat(issueFromService).isEqualTo(buildJiraIssue());
    }

    private JiraIssue buildJiraIssue() {
        var issueType = new JiraIssue.Issuetype( "Task");
        var contentOfContents = List.of( new JiraIssue.ContentOfContent("text", "test ContentOfContent"));
        var contents = List.of(new JiraIssue.Content(("text"), contentOfContents));
        var description = new JiraIssue.Description("text", 123, contents);
        var project = new JiraIssue.Project("JAVA");
        var status = new JiraIssue.Status("NEW");

        return new JiraIssue("Key", "2025-11-15", "assignee",
                new JiraIssue.Fields(
                        issueType,
                        description,
                        project,
                        "Test summary", status));
    }

    private JiraIssueDto buildJiraIssueDto() {
        return new JiraIssueDto(
                "JAVA-1",
                "Test summary",
                "test ContentOfContent",
                "2025-11-15",
                "assignee",
                "Task");
    }
}
