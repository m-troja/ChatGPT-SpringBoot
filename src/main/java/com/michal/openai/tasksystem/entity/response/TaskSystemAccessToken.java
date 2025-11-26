package com.michal.openai.tasksystem.entity.response;

import java.time.LocalDateTime;

public record TaskSystemAccessToken(
        String token,
        LocalDateTime expires

) {
}
