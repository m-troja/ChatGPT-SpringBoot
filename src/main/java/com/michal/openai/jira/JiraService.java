package com.michal.openai.jira;

import java.util.List;
import com.michal.openai.entity.JiraIssue;

public interface JiraService {
	
	String getIssueJson(String id);
	List<JiraIssue> getIssues();
	
}
