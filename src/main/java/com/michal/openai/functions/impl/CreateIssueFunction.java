package com.michal.openai.functions.impl;

import java.util.concurrent.CompletableFuture;

import com.michal.openai.functions.Function;
import com.michal.openai.jira.JiraService;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class CreateIssueFunction implements Function 
{
	private JiraService jiraService;

	@Override
	public String execute(String requestBody) {
        log.info(" execute CreateIssueFunction with arguments: {}", requestBody);
		
	
		return jiraService.createJavaIssue(requestBody);
	}
	
}
