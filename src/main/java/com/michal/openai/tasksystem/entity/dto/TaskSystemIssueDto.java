package com.michal.openai.tasksystem.entity.dto;

public record TaskSystemIssueDto(
        String key,
        String title,
        String description,
        String status,
        String priority,
        String authorSlackId,
        String assigneeSlackId) {
}
