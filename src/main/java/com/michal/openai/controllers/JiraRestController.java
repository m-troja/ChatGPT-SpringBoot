package com.michal.openai.controllers;

import com.michal.openai.jira.entity.JiraCnv;
import com.michal.openai.jira.entity.JiraCreateIssueResponse;
import com.michal.openai.jira.entity.JiraIssueDto;
import com.michal.openai.jira.service.JiraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/v1/jira")
@RequiredArgsConstructor
public class JiraRestController {

	private final JiraService jiraService;
	private final JiraCnv jiraCnv;

	@GetMapping(value = "issue/{id}")
	public JiraIssueDto getIssue(@PathVariable String id)
	{
        log.debug("GET /api/v1/jira/issue, id: {}", id);
        return jiraCnv.convertIssueToIssueDto(jiraService.getIssue(id));
    }

	@GetMapping("issues-java")
	public List<JiraIssueDto> getIssues() {
		log.debug("GET /api/v1/jira/issues-java");
		return jiraCnv.convertListOfIssuesToListOfIssueDto(jiraService.getIssues());
	   
	}
	
	@PostMapping("create-issue")
	public JiraCreateIssueResponse createIssue(@RequestBody String requestBody) {
        log.debug("POST /api/v1/jira/create-issue: {}", requestBody);
		 return jiraService.createJavaIssue(requestBody);
	}
}
