package com.michal.openai.gpt;

import java.util.concurrent.CompletableFuture;

import com.michal.openai.entity.GptFunction;
import com.michal.openai.entity.GptRequest;
import com.michal.openai.entity.GptResponse;

public interface GptService {
	
	 CompletableFuture<String> getAnswerToSingleQuery(String query, String userName, GptFunction... functions);
	 CompletableFuture<String> getAnswerToSingleQuery(CompletableFuture<String> query, GptFunction... gptFunctions);
	 void saveGptRequest(GptRequest request);
	 void saveResponse(GptResponse gptResponse);
}
