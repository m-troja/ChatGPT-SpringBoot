package com.michal.openai.gpt;

import org.springframework.stereotype.Service;

public interface GptService {
	
	public String getAnswerToSingleQuery(String query);
	
}
