package com.michal.openai.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.michal.openai.gpt.GptService;

@RestController
public class ChatGptApiController {
	
	@Autowired
	private GptService gptService;
	
	@GetMapping("/v1/ask-gpt")
	public String askGpt(@RequestParam String query)
	{
		return gptService.getAnswerToSingleQuery(query);
	}

}
