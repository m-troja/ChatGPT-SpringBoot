package com.michal.openai.Controllers;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.michal.openai.entity.JiraIssue;
import com.michal.openai.jira.JiraService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
@RequiredArgsConstructor
public class JiraApiController {

	private final JiraService jiraService;

	@GetMapping(value = "/v1/jira/issue", params = "id")
	public JiraIssue getIssue(@RequestParam("id") String issueId)
	{
        log.debug("GET /v1/jira/issue, id: {}", issueId);
        var jiraIssue = jiraService.getIssue(issueId);
		return jiraIssue;
    }

	@GetMapping("/v1/jira/issues-java")
	public List<JiraIssue> getIssues() {
		log.debug("GET /v1/jira/issues-java");
		 return jiraService.getIssues();
	   
	}
	
	@PostMapping("/v1/jira/create-issue")
	public JiraIssue createIssue(@RequestBody String requestBody) {
        log.debug("POST /v1/jira/create-issue: {}", requestBody);
		 return jiraService.createJavaIssue(requestBody);
	   
	}
}
