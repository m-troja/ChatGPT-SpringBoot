package com.michal.openai.tasksystem.entity.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record TaskSystemIssueDto(
        int id,
        String key,
        String title,
        String teamName,
        String description,
        String status,
        String priority,
        String authorSlackId,
        String assigneeSlackId,
        OffsetDateTime createdAt,
        OffsetDateTime  dueDate,
        OffsetDateTime  updatedAt,
        List<TaskSystemCommentDto> comments,
        int projectId
) {
}
