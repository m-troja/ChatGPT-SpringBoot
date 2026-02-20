package com.michal.openai.tasksystem.service;

import com.michal.openai.tasksystem.entity.TaskSystemEventType;
import com.michal.openai.tasksystem.entity.dto.TaskSystemUserDto;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.entity.response.TaskSystemEvent;

import java.util.List;

public interface TaskSystemService {

    TaskSystemIssueDto createIssue(String requestBody) throws Exception;
    TaskSystemIssueDto assignIssue(String requestBody) throws Exception;
    List<TaskSystemIssueDto> getAllIssues();
    TaskSystemUserDto getTaskSystemUser(String username);
    TaskSystemUserDto getTaskSystemUserAuthorized(String username);
    void parseTaskSystemEvent(TaskSystemEvent event);

}
