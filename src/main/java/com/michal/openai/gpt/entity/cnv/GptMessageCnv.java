package com.michal.openai.gpt.entity.cnv;

import com.michal.openai.gpt.entity.GptMessage;
import com.michal.openai.gpt.entity.dto.RequestDto;
import com.michal.openai.gpt.entity.dto.ResponseDto;
import com.michal.openai.slack.entity.SlackUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GptMessageCnv {

    public RequestDto requestEntityToDto(GptMessage gptMessage, SlackUser slackUserRequestAuthor) {
        var dto = new RequestDto(
                slackUserRequestAuthor.getSlackUserId(),
                slackUserRequestAuthor.getSlackName(),
                gptMessage.getRole(),
                gptMessage.getContent()
        );
        log.debug("Converted requestDto: {}", dto);
        return dto;
    }
    public ResponseDto responseEntityToDto(GptMessage gptMessage, SlackUser slackUserRequestAuthor) {
        var dto = new ResponseDto(
                gptMessage.getContent(),
                slackUserRequestAuthor.getSlackUserId(),
                slackUserRequestAuthor.getSlackName(),
                gptMessage.getRole()
        );
        log.debug("Converted responseDto: {}", dto);
        return dto;
    }
}
