package com.michal.openai.functions.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.Controllers.SlackApiController;
import com.michal.openai.functions.Function;
import com.michal.openai.jira.JiraService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetAllJiraIssues implements Function {

	@Autowired
	JiraService jiraService;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Override
	public CompletableFuture<String> execute(String arguments) {
		log.info("GetAllJiraIssues arguments: " +  arguments);
		 return jiraService.getIssues()
                 .thenApply(
                		 issues -> 
		                 {
							try {
								return objectMapper.writeValueAsString(issues);
							} 
							catch (JsonProcessingException e) {
								e.printStackTrace();
							}
							return null;
						}
		              );	}
	
	

}
