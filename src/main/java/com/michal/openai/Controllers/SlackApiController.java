package com.michal.openai.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.michal.openai.entity.SlackRequestData;
import com.michal.openai.slack.SlackService;

@RequestMapping(value = "/v1/slack")
@RestController
public class SlackApiController {
	
	@Autowired
	Gson gson;
	
	@Autowired
	SlackService slackService;
	
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
	
	@PostMapping
	public String doPost(@RequestBody String requestBody) 
	{
		System.out.println("Slack event received: \n" + requestBody);

		slackService.processOnMentionEven(requestBody);
		
		
		return "";
	}
	
	@GetMapping
	public String doGet() {
		return "Hello";
	}

}
