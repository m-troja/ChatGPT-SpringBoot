package com.michal.openai.jira.service;

import com.michal.openai.jira.entity.JiraCreateIssueResponse;
import com.michal.openai.jira.entity.JiraIssue;

import java.util.List;

public interface JiraService {

    JiraIssue getIssue(String id);
    List<JiraIssue> getIssues();
    JiraCreateIssueResponse createJavaIssue(String requestBody);
	
}
