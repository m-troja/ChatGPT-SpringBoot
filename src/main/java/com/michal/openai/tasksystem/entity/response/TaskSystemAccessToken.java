package com.michal.openai.tasksystem.entity.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record TaskSystemAccessToken(
        String token,
        OffsetDateTime expires

) {
}
