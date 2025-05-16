package com.michal.openai.gpt;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.michal.openai.entity.GptFunction;
import com.michal.openai.entity.GptMessage;
import com.michal.openai.entity.GptRequest;
import com.michal.openai.entity.GptResponse;
import com.michal.openai.entity.SlackUser;

public interface GptService {
	
	 CompletableFuture<String> getAnswerToSingleQuery(CompletableFuture<String> query, CompletableFuture<String> userName, GptFunction... functions);
	 CompletableFuture<String> getAnswerToSingleQuery(CompletableFuture<String> query, GptFunction... gptFunctions);
	 void saveGptRequest(GptRequest request);
	 void saveResponse(GptResponse gptResponse);
	 public List<GptMessage> getLastRequestsOfUser(SlackUser user);
}
