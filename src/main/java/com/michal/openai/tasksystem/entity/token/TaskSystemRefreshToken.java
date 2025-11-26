package com.michal.openai.tasksystem.entity.token;

import java.time.LocalDateTime;

public record TaskSystemRefreshToken(
        String refreshToken,
        int userId,
        LocalDateTime expires,
        boolean isRevoked
) {
}
