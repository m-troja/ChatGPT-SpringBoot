package com.michal.openai.entity;

// Object of info delivered from Slack API
public record SlackRequestData(String messageAuthorId, String message, String channelIdFrom) {}
