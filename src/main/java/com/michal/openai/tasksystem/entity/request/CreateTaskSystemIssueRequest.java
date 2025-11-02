package com.michal.openai.tasksystem.entity.request;

public record CreateTaskSystemIssueRequest(
        String title,
        String description,
        String priority,
        String authorSlackId,
        String assigneeSlackId,
        String dueDate,
        int projectId
) {}
