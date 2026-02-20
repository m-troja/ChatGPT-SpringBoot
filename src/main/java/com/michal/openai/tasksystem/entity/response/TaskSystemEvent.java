package com.michal.openai.tasksystem.entity.response;

import com.michal.openai.tasksystem.entity.TaskSystemEventType;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;

public record TaskSystemEvent(
        TaskSystemEventType event,
        TaskSystemIssueDto issue
)
{
}
