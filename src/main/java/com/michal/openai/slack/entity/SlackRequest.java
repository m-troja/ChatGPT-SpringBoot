package com.michal.openai.slack.entity;

import java.util.List;

public record SlackRequest(
    String token,
    String teamId,
    String apiAppId,
    Event event,
    boolean isExtSharedChannel,
    String eventContext,
    String type,
    String eventId,
    String eventTime,
    List<Authorization> authorizations
){
    public record Event(
        String user,
        String type,
        String ts,
        String clientMsgId,
        String text,
        String team,
        List<Block> blocks,
        String channel,
        String eventTs
    ){}

    public record Block (
        String type,
        String blockId,
        List<Element> elements
    ){}

    public record Element (
        String type,
        String userId,
        String text
    ){}

    public record Authorization (
        String enterpriseId,
        String teamId,
        String userId,
        boolean isBot,
        boolean isEnterpriseInstall
    ){}

}
