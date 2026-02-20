package com.michal.openai.slack.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.functions.entity.GptFunction;
import com.michal.openai.persistence.SlackRepo;
import com.michal.openai.slack.entity.SlackRequest;
import com.michal.openai.slack.entity.SlackUser;
import com.michal.openai.gpt.GptService;
import com.michal.openai.slack.SlackService;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.users.UsersListRequest;
import com.slack.api.methods.response.users.UsersListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class SlackServiceImpl implements SlackService {

    public static final String SUCCESSFULL_REGISTRATION_MESSAGE = "User is registered!";
	private final MethodsClient slackBotClient;
	private final List<GptFunction> functions;
    private final ObjectMapper objectMapper;
    private final SlackRepo jpaSlackrepo;
    private final GptService gptService;
    public static String slackChannelId ;

	@Async("defaultExecutor")
	@Override
    public void processOnMentionEvent(String requestBody) {
        SlackRequest slackRequest;
        try {
            slackRequest = objectMapper.readValue(requestBody, SlackRequest.class);
            checkSlackUserInDb(slackRequest.event().user());
        } catch (JsonProcessingException e) {
            log.error("Error extracting Slack Request: ", e);
            throw new RuntimeException(e);
        }

        CompletableFuture<String> gptResponseFuture = gptService.getAnswerWithSlack(
                CompletableFuture.completedFuture(slackRequest.event().text()),
                CompletableFuture.completedFuture(slackRequest.event().user()),
                functions.toArray(GptFunction[]::new)
        );
        sendMessageToSlack(gptResponseFuture, slackChannelId);
    }

    private void checkSlackUserInDb(String slackUserId) {
        if (jpaSlackrepo.findBySlackUserId(slackUserId) == null) {
            extractUsersIntoDatabase();
        }
    }

    private void extractUsersIntoDatabase() {
			try {
				log.debug("Calling extractUsersIntoDatabase");
				UsersListRequest usersListRequest = UsersListRequest.builder().build();
				UsersListResponse userListResponse = slackBotClient.usersList(usersListRequest);
                userListResponse.getMembers().forEach( user -> {
                    if ( getSlackUserBySlackId(user.getId()) == null) {
                        registerUser(new SlackUser( user.getId(),user.getRealName() ));
                    }
                });
			}
			catch (Exception e) {
                log.error("Error registering users: ", e);
			}
	}

	public void sendMessageToSlack(CompletableFuture<String> message, String channelId) {
		
		message.thenAccept
		(
				response -> {
					String stringToSend = response != null ? response : "[No response]";
					ChatPostMessageRequest request = ChatPostMessageRequest.builder()
//	                .channel(channelId)
	                .channel("test")
	                .text(stringToSend)
	                .build();

			        try {
                        log.info("sendMessageToSlack: Channel= {}, Message= {}", channelId, stringToSend);
			            slackBotClient.chatPostMessage(request);
			        }
			        catch (IOException | SlackApiException e){
                        log.error(e.getMessage());
			        }
                }
		)
		.exceptionally(ex -> {
                log.error("Error in GPT response future: {}", ex.getMessage());
			       return null;
			}
		);
	}
    public void sendMessageToSlack(String message, String slackChannelId)
    {
        sendMessageToSlack(CompletableFuture.completedFuture(message), slackChannelId);
    }

    @Override
    public void triggerGetUsers() {
        extractUsersIntoDatabase();
    }

    public SlackUser getSlackUserBySlackId(String slackId) {
		return jpaSlackrepo.findBySlackUserId(slackId);
	}
	
	public List<SlackUser> getAllSlackUsers() 	{
		List<SlackUser> users = jpaSlackrepo.findAllByOrderBySlackUserId();
        log.debug("All slack users: {}", users);
		return users;
	}

    @Override
	public String registerUser(SlackUser user) {
        jpaSlackrepo.save(user);
        log.info("Registered user: {}", user);
        return SUCCESSFULL_REGISTRATION_MESSAGE  ;
    }
    public SlackServiceImpl(@Qualifier("slackBotClient") MethodsClient slackBotClient, List<GptFunction> functions, ObjectMapper objectMapper, SlackRepo jpaSlackrepo, GptService gptService) {
        this.slackBotClient = slackBotClient;
        this.functions = functions;
        this.objectMapper = objectMapper;
        this.jpaSlackrepo = jpaSlackrepo;
        this.gptService = gptService;
        slackChannelId = System.getenv().getOrDefault("SLACK_CHANNEL_ID", "C08RLDBCRB9");
        log.info("SLACK_CHANNEL_ID = {}", slackChannelId);
    }
}
