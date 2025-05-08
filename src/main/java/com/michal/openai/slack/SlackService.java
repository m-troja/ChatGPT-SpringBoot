package com.michal.openai.slack;

public interface SlackService {

	void processOnMentionEvent(String requestBody);
}
