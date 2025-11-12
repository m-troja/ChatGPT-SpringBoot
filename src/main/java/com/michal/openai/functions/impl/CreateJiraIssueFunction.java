package com.michal.openai.functions.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.functions.Function;
import com.michal.openai.jira.JiraService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
public class CreateJiraIssueFunction implements Function
{
	private final JiraService jiraService;
    private ObjectMapper objectMapper;

	@Override
	public String execute(String requestBody) {

        log.debug(" execute CreateIssueFunction with arguments: {}", requestBody);
	    var jiraIssue = jiraService.createJavaIssue(requestBody);
	    log.debug("Created JiraIssueDto: {}", jiraIssue);
        String json;
        try {
            json = objectMapper.writeValueAsString(jiraIssue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
	}
	
}
