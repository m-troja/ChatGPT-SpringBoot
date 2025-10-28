package com.michal.openai.Controllers;

import com.michal.openai.entity.SlackUser;
import com.michal.openai.slack.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/all")
public class UserController {

    private final SlackService slackService;

    @GetMapping
    public List<SlackUser> getAllSlackUsers() {
        log.debug("Received request to GET /api/v1/users/all");
        return slackService.getAllSlackUsers();
    }
}
