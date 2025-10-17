package com.michal.openai.entity;

public record SlackRequestData(String messageAuthorId, String message, String channelIdFrom) {}
