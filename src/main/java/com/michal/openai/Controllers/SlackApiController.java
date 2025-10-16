package com.michal.openai.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.slack.SlackService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping
@RestController
public class SlackApiController {
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	SlackService slackService;
	

//	@PostMapping(value = "/v1/slack")
//	public String doPost(@RequestBody String requestBody)
//	{
//		log.debug("POST /v1/slack " + requestBody);
//
//		slackService.processOnMentionEvent(requestBody);
//
//		log.debug("********** Responded status 200 to Slack **********");
//		return "OK";
//	}

	
//	Authentication
//	
	@PostMapping(value = "/v1/slack")
	public String returnChallengeValue(@RequestBody String requestBody)
	{
		System.out.println(requestBody);
		try
		{
			JsonNode mainNode = objectMapper.readTree(requestBody);
			JsonNode challengeNode = mainNode.path("challenge");
			System.out.println("/v1/slack: challenge = " + challengeNode.asText());

			return challengeNode.asText();
		}
		catch (Exception e)
		{
			return "error";
		}
	}
	
}
