package com.michal.openai.gpt;

import java.util.concurrent.CompletableFuture;

import com.michal.openai.entity.GptFunction;

public interface GptService {
	
	 CompletableFuture<String> getAnswerWithSlack(CompletableFuture<String> query, CompletableFuture<String> userName, GptFunction... functions  );
}
