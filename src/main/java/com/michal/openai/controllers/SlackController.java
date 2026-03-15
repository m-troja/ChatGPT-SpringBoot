package com.michal.openai.controllers;

import com.michal.openai.gpt.service.SlackGptCoordinator;
import com.michal.openai.slack.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/v1/slack")
@RequiredArgsConstructor
@RestController
public class SlackController {

	private final SlackGptCoordinator slackGptCoordinator;
    private final SlackService slackService;

    @PostMapping
    public ResponseEntity<String> doPost(@RequestBody String requestBody) {
        log.info("Received POST /api/v1/slack");
        log.debug("Received body: {}", requestBody);

        slackGptCoordinator.processMention(requestBody);

        log.debug("********** Responded status 200 to Slack **********");
        return ResponseEntity.ok("OK");
    }


    @GetMapping("get-users")
    public HttpStatus getUsers()
    {
        log.info("Received GET /api/v1/slack/get-users");

        slackService.triggerGetUsers();
        return HttpStatus.OK;
    }

//	Authentication
//
//	@PostMapping(value = "/api/v1/slack")
//	public String returnChallengeValue(@RequestBody String requestBody)
//	{
//		System.out.println(requestBody);
//		try
//		{
//			JsonNode mainNode = objectMapper.readTree(requestBody);
//			JsonNode challengeNode = mainNode.path("challenge");
//			System.out.println("/api/v1/slack: challenge = " + challengeNode.asText());
//
//			return challengeNode.asText();
//		}
//		catch (Exception e)
//		{
//			return "error";
//		}
//	}

}
