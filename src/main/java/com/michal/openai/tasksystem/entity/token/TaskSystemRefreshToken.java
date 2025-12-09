package com.michal.openai.tasksystem.entity.token;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record TaskSystemRefreshToken(
        String token,
        OffsetDateTime expires
) {}
