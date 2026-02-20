package com.michal.openai.slack;

import com.michal.openai.slack.entity.SlackUser;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SlackService {

    void processOnMentionEvent(String requestBody);

    String registerUser(SlackUser user);

    SlackUser getSlackUserBySlackId(String slackid);

    List<SlackUser> getAllSlackUsers();

    void sendMessageToSlack(CompletableFuture<String> message, String channelId);
    void sendMessageToSlack(String message, String channelId);
    void triggerGetUsers();
}
