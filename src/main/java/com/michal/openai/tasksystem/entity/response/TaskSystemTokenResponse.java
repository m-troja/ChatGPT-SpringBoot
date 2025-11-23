package com.michal.openai.tasksystem.entity.response;

import com.michal.openai.tasksystem.entity.token.TaskSystemRefreshToken;

public record TaskSystemTokenResponse(
        TaskSystemAccessToken accessToken,
        TaskSystemRefreshToken refreshToken
) {}