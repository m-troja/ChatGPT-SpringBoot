package com.michal.openai.tasksystem.service;

import com.michal.openai.gpt.tool.tools.AssignTaskSystemIssueTool;
import com.michal.openai.gpt.tool.tools.CreateTaskSystemIssueTool;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.entity.dto.TaskSystemUserDto;
import com.michal.openai.tasksystem.entity.response.TaskSystemEvent;

import java.util.List;

public interface TaskSystemService {

    TaskSystemIssueDto createIssue(CreateTaskSystemIssueTool.Args args);
    TaskSystemIssueDto assignIssue(AssignTaskSystemIssueTool.Args requestBody) ;
    List<TaskSystemIssueDto> getAllIssues();
    List<TaskSystemIssueDto> getIssuesForUser(String requestBody);
    TaskSystemUserDto getTaskSystemUser(String username);
    TaskSystemUserDto getTaskSystemUserAuthorized(String username);
    void parseTaskSystemEvent(TaskSystemEvent event);

}
