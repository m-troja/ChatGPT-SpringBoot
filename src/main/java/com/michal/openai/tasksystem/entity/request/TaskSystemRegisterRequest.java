package com.michal.openai.tasksystem.entity.request;

public record TaskSystemRegisterRequest(
        String FirstName,
        String LastName,
        String Email,
        String Password,
        String SlackUserId
) {
}
