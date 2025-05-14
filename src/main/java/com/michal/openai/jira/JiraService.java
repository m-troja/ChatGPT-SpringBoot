package com.michal.openai.jira;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.michal.openai.entity.JiraIssue;

public interface JiraService {
	
	String getIssueJson(String id);
	CompletableFuture<List<JiraIssue>> getIssues();
	CompletableFuture<String> createJavaIssue(String requestBody);
	
}
