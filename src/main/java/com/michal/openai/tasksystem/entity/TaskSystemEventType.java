package com.michal.openai.tasksystem.entity;

public enum TaskSystemEventType {
    ISSUE_CREATED,
    ISSUE_ASSIGNED,
    ISSUE_DELETED,
    COMMENT_CREATED,
    UPDATE_DUEDATE,
    UPDATE_PRIORITY,
    UPDATE_STATUS
}
