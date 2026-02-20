package com.michal.openai.tasksystem.entity.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.util.List;

public record TaskSystemIssueDto(
        int id,
        String key,
        String title,
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
