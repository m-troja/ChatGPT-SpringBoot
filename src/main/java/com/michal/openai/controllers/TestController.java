package com.michal.openai.controllers;

import com.michal.openai.tasksystem.entity.dto.TaskSystemUserDto;
import com.michal.openai.tasksystem.service.TaskSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final TaskSystemService taskSystemService;

    @GetMapping("task-system/user/unauthorized/{slackUserId}")
    public TaskSystemUserDto getUserBySlackUserId(@PathVariable String slackUserId) {
        log.debug("Received unauthorized GET task-system/user/{}", slackUserId);
        return taskSystemService.getTaskSystemUser(slackUserId);
    }

    @GetMapping("task-system/user/authorized/{slackUserId}")
    public TaskSystemUserDto getUserBySlackUserIdAuthorized(@PathVariable String slackUserId) {
        log.debug("Received authorized GET task-system/user/{}", slackUserId);
        return taskSystemService.getTaskSystemUserAuthorized(slackUserId);
    }
}
