package com.michal.openai.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.tasksystem.entity.TaskSystemEventType;
import com.michal.openai.tasksystem.entity.dto.TaskSystemUserDto;
import com.michal.openai.tasksystem.entity.response.TaskSystemEvent;
import com.michal.openai.tasksystem.service.TaskSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/taskstorm")
@RequiredArgsConstructor
public class TestController {

    private final TaskSystemService taskSystemService;

    @GetMapping("user/unauthorized/{slackUserId}")
    public TaskSystemUserDto getUserBySlackUserId(@PathVariable String slackUserId) {
        log.debug("Received unauthorized GET task-system/user/{}", slackUserId);
        return taskSystemService.getTaskSystemUser(slackUserId);
    }

    @GetMapping("user/authorized/{slackUserId}")
    public TaskSystemUserDto getUserBySlackUserIdAuthorized(@PathVariable String slackUserId) {
        log.debug("Received authorized GET task-system/user/{}", slackUserId);
        return taskSystemService.getTaskSystemUserAuthorized(slackUserId);
    }

    @PostMapping("event")
    public HttpStatus parseTaskSystemEvent(@RequestBody TaskSystemEvent event) throws JsonProcessingException {
        log.debug("Received POST parseTaskSystemEvent {}", event);
        var om = new ObjectMapper();
        taskSystemService.parseTaskSystemEvent(event);
        return HttpStatus.OK;
    }
}
