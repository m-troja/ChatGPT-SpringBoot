package com.michal.openai.slack;

import com.michal.openai.entity.SlackUser;

import java.util.List;

public interface SlackService {

	void processOnMentionEvent(String requestBody);

	String registerUser(SlackUser user);
	SlackUser getSlackUserBySlackId(String slackid);
	List<SlackUser> getAllSlackUsers();
}
