package com.michal.openai.Controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.michal.openai.entity.GptFunction;
import com.michal.openai.gpt.GptService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
public class ChatGptApiController {
	
	private final GptService gptService;
    private final List<GptFunction> functions;
	
	@GetMapping("/v1/ask-gpt")
	public CompletableFuture<String> askGpt(@RequestParam String query, @RequestParam String userSlackId)
	{
		log.info("GET /v1/ask-gpt with params: query={}, userSlackId={}", query, userSlackId );
		return gptService.getAnswerWithSlack(CompletableFuture.completedFuture(query), CompletableFuture.completedFuture(userSlackId), functions.toArray(GptFunction[]::new));
	}

    public ChatGptApiController(GptService gptService, List<GptFunction> functions) {
        this.gptService = gptService;
        this.functions = functions;
    }
}
