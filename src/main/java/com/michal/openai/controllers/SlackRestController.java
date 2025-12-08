package com.michal.openai.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.slack.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/v1/slack")
@RequiredArgsConstructor
@RestController
public class SlackRestController {

	private final SlackService slackService;
	private final ObjectMapper objectMapper;

//	@PostMapping
//	public String doPost(@RequestBody String requestBody)
//	{
//        log.info("Received POST /api/v1/slack");
//        log.debug("Received POST /api/v1/slack with body: {} \n", requestBody);
//
//		slackService.processOnMentionEvent(requestBody);
//
//		log.debug("********** Responded status 200 to Slack **********");
//		return "OK";
//	}

//	Authentication
//
	@PostMapping
	public String returnChallengeValue(@RequestBody String requestBody)
	{
		System.out.println(requestBody);
		try
		{
			JsonNode mainNode = objectMapper.readTree(requestBody);
			JsonNode challengeNode = mainNode.path("challenge");
			System.out.println("/api/v1/slack: challenge = " + challengeNode.asText());

			return challengeNode.asText();
		}
		catch (Exception e)
		{
			return "error";
		}
	}

}
