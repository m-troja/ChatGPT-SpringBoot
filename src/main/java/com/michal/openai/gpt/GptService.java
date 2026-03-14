package com.michal.openai.gpt;

import com.michal.openai.functions.entity.GptFunction;

import java.util.concurrent.CompletableFuture;

public interface GptService {
	
	String getAnswerWithSlack(String query,String userName );
    void clearDatabase();
}
