package com.michal.openai.gpt;

import com.michal.openai.entity.GptFunction;

public interface GptService {
	
	public String getAnswerToSingleQuery(String query, GptFunction... functions);
	
}
