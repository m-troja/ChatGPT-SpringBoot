package com.michal.openai.jira;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.michal.openai.entity.JiraIssue;

public interface JiraService {
	
	JiraIssue getIssue(String id);
    List<JiraIssue> getIssues();
    JiraIssue createJavaIssue(String requestBody);
	
}
