package com.michal.openai.functions.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.michal.openai.functions.Function;
import com.michal.openai.jira.JiraService;

public class GetAllJiraIssues implements Function {

	@Autowired
	JiraService jiraService;
	
	@Autowired
	Gson gson;
	
	@Override
	public String execute(String arguments) {
		System.out.println("GetAllJiraIssues jiraService.getIssues(): " +  gson.toJson(jiraService.getIssues()));
		 return gson.toJson(jiraService.getIssues() );
	}
	
	

}
