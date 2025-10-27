package com.michal.openai.tasksystem.service;

import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TaskSystemService {

    CompletableFuture<String> createIssue(String requestBody) throws Exception;
    List<TaskSystemIssueDto> getAllIssues();
}
