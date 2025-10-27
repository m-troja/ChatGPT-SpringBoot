package com.michal.openai.functions.impl;

import com.michal.openai.functions.Function;
import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;
import com.michal.openai.tasksystem.service.TaskSystemService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public class CreateTaskSystemIssueFunction implements Function
{
	private final TaskSystemService taskSystemService;

	@Override
	public CompletableFuture<String> execute(String requestBody) {
        log.info("Execute CreateTaskSystemIssueFunction with requestBody: {}", requestBody);
        try {
            return taskSystemService.createIssue(requestBody).exceptionally( ex -> {
                log.error("Failed to create Task-System issue",  ex);
                throw new RuntimeException("Failed to create Task-System issue");
                });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
