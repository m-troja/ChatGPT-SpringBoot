package com.michal.openai.jira.entity;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JiraCnv {

    public JiraIssueDto convertIssueToIssueDto(JiraIssue jiraIssue) {
        return new JiraIssueDto(
                jiraIssue.key(),
                jiraIssue.fields().summary(),
                jiraIssue.fields().description().content().getFirst().contentOfContent().getFirst().text(),
                jiraIssue.dueDate(),
                jiraIssue.assignee(),
                jiraIssue.fields().issueType().name()
        );
    }

    public List<JiraIssueDto> convertListOfIssuesToListOfIssueDto(List<JiraIssue> issues) {
        return issues.stream().map(this::convertIssueToIssueDto).toList();
    }
}
