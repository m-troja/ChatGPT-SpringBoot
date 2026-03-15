package com.michal.openai.tasksystem.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateTaskSystemIssueRequest(
        String title,
        String description,
        String priority,
        @JsonProperty("authorslackid") String authorSlackId,
        @JsonProperty("assigneeslackid") String assigneeSlackId,
        @JsonProperty("duedate") String dueDate,
        @JsonProperty("projectid") int projectId
) {}
