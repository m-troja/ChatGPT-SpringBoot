package com.michal.openai.Controllers;

import com.michal.openai.slack.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping
@RequiredArgsConstructor
@RestController
public class SlackApiController {

	private final SlackService slackService;
	
	@PostMapping(value = "/v1/slack")
	public String doPost(@RequestBody String requestBody)
	{
        log.info("Received POST /v1/slack");
        log.debug("Received POST /v1/slack with body: {} \n", requestBody);

		slackService.processOnMentionEvent(requestBody);

		log.debug("********** Responded status 200 to Slack **********");
		return "OK";
	}

//	Authentication
//
//	@PostMapping(value = "/v1/slack")
//	public String returnChallengeValue(@RequestBody String requestBody)
//	{
//		System.out.println(requestBody);
//		try
//		{
//			JsonNode mainNode = objectMapper.readTree(requestBody);
//			JsonNode challengeNode = mainNode.path("challenge");
//			System.out.println("/v1/slack: challenge = " + challengeNode.asText());
//
//			return challengeNode.asText();
//		}
//		catch (Exception e)
//		{
//			return "error";
//		}
//	}

}
