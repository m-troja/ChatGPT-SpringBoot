package com.michal.openai.tasksystem.service;

import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;

import java.util.List;

public interface TaskSystemService {

    TaskSystemIssueDto createIssue(String requestBody) throws Exception;
    TaskSystemIssueDto assignIssue(String requestBody) throws Exception;
    List<TaskSystemIssueDto> getAllIssues();
}
