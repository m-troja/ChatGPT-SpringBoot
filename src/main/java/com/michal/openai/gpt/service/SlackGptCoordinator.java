package com.michal.openai.gpt.service;

import com.michal.openai.slack.SlackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SlackGptCoordinator {

    private final SlackService slackService;
    private final GptService gptService;
    private final String slackChannelId;

    public SlackGptCoordinator(SlackService slackService, GptService gptService) {
        this.slackService = slackService;
        this.gptService = gptService;
        this.slackChannelId =
                System.getenv().getOrDefault("SLACK_CHANNEL_ID", "C08RLDBCRB9");

    }

    @Async("defaultExecutor")
    public void processMention(String text) {
        try {
            var slackUserId = slackService.processOnMentionEvent(text);
            String response = gptService.getAnswerWithSlack(text, slackUserId);
            slackService.sendMessageToSlack(response, slackChannelId);
        } catch (Exception e) {
            log.error("Error processing Slack mention asynchronously", e);
        }
    }
}