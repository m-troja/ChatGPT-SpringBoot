package com.michal.openai.tasksystem.entity.request;

public record TaskSystemLoginRequest(
        String email,
        String password
) {
}
