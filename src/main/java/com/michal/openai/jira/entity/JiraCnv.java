package com.michal.openai.jira.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class JiraCnv {

    public JiraIssueDto convertIssueToIssueDto(JiraIssue jiraIssue) {

        var dto = new JiraIssueDto(
                jiraIssue.key(),
                jiraIssue.fields().summary(),
                jiraIssue.fields().description().content().getFirst().contentOfContent().getFirst().text(),
                jiraIssue.dueDate(),
                jiraIssue.assignee(),
                jiraIssue.fields().issueType().name()
        );
        log.debug("Converted {} to {}", jiraIssue, dto);
        return dto;
    }

    public List<JiraIssueDto> convertListOfIssuesToListOfIssueDto(List<JiraIssue> issues) {
        return issues.stream().map(this::convertIssueToIssueDto).toList();
    }
}
