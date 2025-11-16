package com.michal.openai.functions.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.functions.Function;
import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;
import com.michal.openai.tasksystem.service.TaskSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignTaskSystemIssueFunction implements Function
{
	private final TaskSystemService taskSystemService;
    private final ObjectMapper objectMapper;
	@Override
	public String execute(String requestBody) {

        log.debug("Execute AssignTaskSystemIssueFunction with requestBody: {}", requestBody);

        TaskSystemIssueDto dto;
       
        try {
            dto = taskSystemService.assignIssue(requestBody);
        } catch (Exception e) {
            log.error("Error assigning task-system issue ", e);
            throw new RuntimeException(e);
        }

        String json;

        try {
            json = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("Error creating task-system assign-issue result json ", e);
            throw new RuntimeException(e);
        }

        return json ;
    }
}
