package com.michal.openai.tasksystem.entity;

import com.google.api.client.util.DateTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record TaskSystemRefreshToken(
        String refreshToken,
        int userId,
        LocalDateTime expires,
        boolean isRevoked
) {
}
