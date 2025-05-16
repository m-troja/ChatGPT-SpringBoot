package com.michal.openai.slack;

import com.michal.openai.entity.SlackUser;

public interface SlackService {

	void processOnMentionEvent(String requestBody);

	String registerUser(SlackUser user);
	public SlackUser getSlackUserBySlackId(String slackid);
}
