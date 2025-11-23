package com.michal.openai.tasksystem.entity.dto;

import java.util.List;

public record TaskSystemUserDto(
        int id,
        String firstName,
        String lastName,
        String email,
        List<String> roles,
        List<String> teams,
        boolean disabled,
        String userSlackId
) {
}
