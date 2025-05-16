package com.michal.openai.Controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.JiraIssue;
import com.michal.openai.jira.JiraService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
@RequestMapping()
public class JiraApiController {

	@Autowired
	JiraService jiraService;
	
	@Autowired
	ObjectMapper objectMapper;
	
	// Example: http://localhost:8080/v1/jira/issue?id=JAVA-6
	@GetMapping(value = "/v1/jira/issue", params = "id")
	public ResponseEntity<String> getIssue(@RequestParam("id") String issueId)
	{		
		log.info("GET /v1/jira/issue, id: " + issueId);
		return ResponseEntity
	            .ok()
	            .contentType(MediaType.APPLICATION_JSON)
	            .body(jiraService.getIssueJson(issueId));	}

	// Example: http://localhost:8080/v1/jira/issues-java
	@GetMapping("/v1/jira/issues-java")
	public CompletableFuture<List<JiraIssue>> getIssues() {
		log.info("GET /v1/jira/issues-java");
		 return jiraService.getIssues();
	   
	}
	
	@PostMapping("/v1/jira/create-issue")
	public CompletableFuture<String> createIssue(@RequestBody String requestBody) {
		log.info("POST /v1/jira/create-issue: "+ requestBody);
		 return jiraService.createJavaIssue(requestBody);
	   
	}
}
