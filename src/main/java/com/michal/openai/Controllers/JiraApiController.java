package com.michal.openai.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.michal.openai.entity.JiraIssue;
import com.michal.openai.jira.JiraService;

@RestController
@RequestMapping()
public class JiraApiController {

	@Autowired
	JiraService jiraService;
	
	@Autowired
	Gson gson;
	
	// Example: http://localhost:8080/v1/jira/issue?id=JAVA-6
	@GetMapping(value = "/v1/jira/issue", params = "id")
	public String getIssue(@RequestParam("id") String issueId)
	{		
		return gson.toJson(jiraService.getIssueJson(issueId));
	}

	// Example: http://localhost:8080/v1/jira/issues-java
	@GetMapping("/v1/jira/issues-java")
	public List<JiraIssue> getIssues() {
		 return jiraService.getIssues();
	   
	}
	
	
//	@GetMapping("/v1/jira/issues-java")
//	public ResponseEntity<String> getIssues() {
//	    String json = jiraService.getIssues();
//	    return ResponseEntity
//	            .ok()
//	            .contentType(MediaType.APPLICATION_JSON)
//	            .body(json);
//	}
}
