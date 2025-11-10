package com.michal.openai.gpt;

import com.michal.openai.entity.GptFunction;

import java.util.concurrent.CompletableFuture;

public interface GptService {
	
	CompletableFuture<String> getAnswerWithSlack(CompletableFuture<String> query, CompletableFuture<String> userName, GptFunction... functions  );
    void clearDatabase();
}
