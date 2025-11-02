package com.michal.openai.functions.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.functions.Function;
import com.michal.openai.tasksystem.service.TaskSystemService;
import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAllTaskSystemIssuesFunction implements Function {

	private final TaskSystemService taskSystemService;
    private final ObjectMapper objectMapper;

	@Override
	public String execute(String arguments) {
        log.info("Executing GetAllTaskSystemIssuesFunction arguments: {}", arguments);
        try {
                List<TaskSystemIssueDto> issues = taskSystemService.getAllIssues();
                log.debug("Fetched {} issues from Task System", issues.size());
                return objectMapper.writeValueAsString(issues);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize issues to JSON", e);
                throw new RuntimeException("Serialization error", e);
            } catch (Exception e) {
                log.error("Unexpected error while fetching issues", e);
                throw new RuntimeException("Failed to fetch Task System issues", e);
            }
    }
}