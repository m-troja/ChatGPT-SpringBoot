package com.michal.openai.tasksystem.entity.response;

import com.michal.openai.tasksystem.entity.TaskSystemRefreshToken;

public record TaskSystemTokenResponse(
        String accessToken,
        TaskSystemRefreshToken refreshToken
) {}