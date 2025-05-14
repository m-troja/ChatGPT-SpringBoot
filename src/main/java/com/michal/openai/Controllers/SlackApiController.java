package com.michal.openai.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.michal.openai.slack.SlackService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping(value = "/v1/slack")
@RestController
public class SlackApiController {
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	SlackService slackService;
	

	@PostMapping
	public String doPost(@RequestBody String requestBody) 
	{
		log.debug("POST /v1/slack " + requestBody);
		
		slackService.processOnMentionEvent(requestBody);
		
		log.debug("********** Responded status 200 to Slack **********");
		return "OK";
	}

	
//	Authentication
//	
//	@PostMapping
//	public String doPost(@RequestBody String requestBody) 
//	{
//		System.out.println(requestBody);
//		JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
//		String challengeValue = jsonObject.get("challenge").getAsString();
//		System.out.println("/v1/slack: challenge = " + challengeValue);
//		return challengeValue;
//	}
	
}
