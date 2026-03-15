package com.michal.openai.gpt.service;

public interface GptService {
	
	String getAnswerWithSlack(String query,String userName );
    void clearDatabase();
}
