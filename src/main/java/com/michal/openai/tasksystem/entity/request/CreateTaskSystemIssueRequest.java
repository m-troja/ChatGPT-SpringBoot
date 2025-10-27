package com.michal.openai.tasksystem.entity.request;

import lombok.Data;

public record CreateTaskSystemIssueRequest(
        String Title,
        String Description,
        String Priority,
        int AuthorId,
        int AssigneeId,
        String DueDate,
        int ProjectId
) {}
