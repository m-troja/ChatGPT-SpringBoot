package com.michal.openai.tasksystem.entity.response;

public record TaskSystemIssueDto(String key, String title, String description, String status, String priority, int authorId, int assigneeId) {
}
