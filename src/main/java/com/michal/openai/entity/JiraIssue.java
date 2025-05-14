package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JiraIssue {
	
	private String key;
	private String summary;
	private String description;
	private String duedate;
	private String assignee;
	@JsonProperty("issuetype")  // force this exact JSON key
	private String issueType;


	public JiraIssue(String key, String summary, String description, String duedate, String assignee) {
		this.key = key;
		this.summary = summary;
		this.description = description;
		this.duedate = duedate;
		this.assignee = assignee;
	}
	
	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}
	
	public JiraIssue() {
		
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDuedate() {
		return duedate;
	}

	public void setDuedate(String duedate) {
		this.duedate = duedate;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	@Override
	public String toString() {
		return "JiraIssue [key=" + key + ", summary=" + summary + ", description=" + description + ", duedate="
				+ duedate + ", assignee=" + assignee + "]";
	}

	
}
