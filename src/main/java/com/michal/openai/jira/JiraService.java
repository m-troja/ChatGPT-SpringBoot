package com.michal.openai.jira;

import com.michal.openai.entity.JiraIssue;

import java.util.List;

public interface JiraService {
	
	JiraIssue getIssue(String id);
    List<JiraIssue> getIssues();
    JiraIssue createJavaIssue(String requestBody);
	
}
