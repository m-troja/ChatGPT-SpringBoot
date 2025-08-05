package com.michal.openai.functions.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;

import com.michal.openai.functions.Function;
import com.michal.openai.jira.JiraService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class CreateIssueFunction implements Function 
{

	@Autowired
	JiraService jiraService;
	
	@Override
	public CompletableFuture<String> execute(String requestBody) {
		log.info(" execute CreateIssueFunction with arguments: " + requestBody);
		
	
		return jiraService.createJavaIssue(requestBody);
	}
	
}
