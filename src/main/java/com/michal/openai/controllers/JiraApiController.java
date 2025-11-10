package com.michal.openai.controllers;

import com.michal.openai.entity.JiraIssue;
import com.michal.openai.jira.JiraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/v1/jira")
@RequiredArgsConstructor
public class JiraApiController {

	private final JiraService jiraService;

	@GetMapping(value = "issue", params = "id")
	public JiraIssue getIssue(@RequestParam("id") String issueId)
	{
        log.debug("GET /api/v1/jira/issue, id: {}", issueId);
        return jiraService.getIssue(issueId);
    }

	@GetMapping("issues-java")
	public List<JiraIssue> getIssues() {
		log.debug("GET /api/v1/jira/issues-java");
		 return jiraService.getIssues();
	   
	}
	
	@PostMapping("create-issue")
	public JiraIssue createIssue(@RequestBody String requestBody) {
        log.debug("POST /api/v1/jira/create-issue: {}", requestBody);
		 return jiraService.createJavaIssue(requestBody);
	}
}
