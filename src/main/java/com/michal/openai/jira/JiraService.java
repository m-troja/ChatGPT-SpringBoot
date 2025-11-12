package com.michal.openai.jira;

import com.michal.openai.jira.entity.JiraIssue;
import com.michal.openai.jira.entity.JiraIssueDto;

import java.util.List;

public interface JiraService {

    JiraIssue getIssue(String id);
    List<JiraIssue> getIssues();
    JiraIssueDto createJavaIssue(String requestBody);
	
}
