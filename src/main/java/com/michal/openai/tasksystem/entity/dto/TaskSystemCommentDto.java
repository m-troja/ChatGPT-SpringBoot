package com.michal.openai.tasksystem.entity.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

public record TaskSystemCommentDto
        (
                int id,
                int issueId,
                String content,
                int authorId,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt,
                String authorName

        ){
}
