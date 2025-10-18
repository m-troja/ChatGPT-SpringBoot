package com.michal.openai.slack.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.GptFunction;
import com.michal.openai.entity.SlackRequestData;
import com.michal.openai.entity.SlackUser;
import com.michal.openai.gpt.GptService;
import com.michal.openai.persistence.JpaSlackRepo;
import com.michal.openai.slack.SlackService;
import com.slack.api.model.User;

import lombok.extern.slf4j.Slf4j;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.users.UsersListRequest;
import com.slack.api.methods.response.users.UsersListResponse;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Service
@Data
public class DefaultSlackService implements SlackService {

	@Qualifier("slackBotClient")
	private MethodsClient slackBotClient;
	public static final String SUCCESSFULL_REGISTRATION_MESSAGE = "User is registered!";
	private static final String REGISTRATION_ERROR_MESSAGE = "Error - user already registered!.";

	private List<GptFunction> functions;
    private ObjectMapper objectMapper;
    private JpaSlackRepo jpaSlackrepo;
    private GptService gptService;

	@Async("defaultExecutor")
	@Override
    public void processOnMentionEvent(String requestBody) {
        SlackRequestData slackRequestData = extractSlackRequestData(requestBody);
        log.debug("processOnMentionEvent requestBody : {}", requestBody);

        CompletableFuture<String> gptResponseFuture = gptService.getAnswerToSingleQuery(
                CompletableFuture.completedFuture(slackRequestData.message()),
                CompletableFuture.completedFuture(slackRequestData.messageAuthorId()),
                functions.toArray(GptFunction[]::new)
        );

        sendMessageToSlack(gptResponseFuture, slackRequestData.channelIdFrom());
    }

	private SlackRequestData extractSlackRequestData(String requestBody) {
		log.info("extractSlackRequestData....");
		JsonNode jsonNode = null;
		
		try {
			jsonNode = objectMapper.readValue(requestBody, JsonNode.class);
		}
		catch(Exception e)
		{
			log.error("Error in jsonObject = objectMapper.readValue(requestBody, JsonObject.class)!");
            log.error(e.getMessage());
        }

        // Extract the 'event' object
        JsonNode eventNode = null;
		try {
			eventNode = jsonNode.path("event");
		} catch (Exception e) {
            log.error(e.getMessage());
		}
        
        // Extract the fields from the 'event' object
        String messageAuthorId = eventNode.path("user").asText();
        String message = eventNode.path("text").asText();
        String channelIdFrom = eventNode.path("channel").asText();
		SlackUser user ;
		try
		{
			// Use a Map to before sending request to Slack API
			if ( getSlackUserBySlackId(messageAuthorId) == null)
			{
				log.info(" messageAuthorId not found! Extracting users...");
				extractUsersIntoDatabase();
				user = getSlackUserBySlackId(messageAuthorId);
                log.info("Slackuser extracted :  {}", user.toString());
			}
		}
		catch( Exception e)
		{
            log.error(e.getMessage());
        }
		String messageWithNames = substituteUserIdsWithUserNames(message);

        log.info("extractSlackRequestData: {} : {} : {}", messageAuthorId, messageWithNames, channelIdFrom);

		return new SlackRequestData(messageAuthorId, messageWithNames, channelIdFrom);
	}

	private String substituteUserIdsWithUserNames(String message) {
		
		// Extract all slackIDs in a message to string array
		String[] userIds = extractUserIds(message);
		
		StringBuilder stringBuilder = new StringBuilder(message);
        log.info("stringBuilder : {}", stringBuilder);

		// For slackID in array of slackIDs, assign realName and switch slackID to realName
		for(String userId : userIds)
		{
			String name = getSlackUserBySlackId(userId).getRealName();
			String mention = "<@" + userId + ">";
			
			if(name != null)
			{
				int startIndex = stringBuilder.indexOf(mention);
				int endIndex = startIndex + mention.length();
                log.info("startIndex : {}, endIndex: {}", startIndex, endIndex);

				stringBuilder.replace(startIndex, endIndex, name);
			}
		}
        log.info("stringBuilder : {}", stringBuilder);

		return stringBuilder.toString();
	}

	private String[] extractUserIds(String message) {
		
		List<String> userIds = new ArrayList<>();
		Pattern pattern = Pattern.compile("<@(\\w+)>");
		Matcher matcher = pattern.matcher(message);
		while (matcher.find()) {
			userIds.add(matcher.group(1));
		}
        log.info("extractUserIds : {}", userIds);

		return userIds.toArray(new String[userIds.size()]);
	}

	private void extractUsersIntoDatabase() {
			try 
			{
				log.info("extractUsersIntoDatabase()...");
				UsersListRequest usersListRequest = UsersListRequest.builder().build();
				UsersListResponse userListResponse = slackBotClient.usersList(usersListRequest);          

				// Users returned by Slack should be inserted into database
				for (User user : userListResponse.getMembers())
				{
					// If user not already registered -> register user
					if ( getSlackUserBySlackId(user.getId()) == null)
					{
						registerUser(new SlackUser( user.getId(),user.getRealName() ));
                        log.info("registerUser : {} : {}", user.getId(), user.getRealName());
					}
				}
			} 
			catch (Exception e) 
			{
                log.error(e.getMessage());
			}
	}
	@Async("defaultExecutor")
	public void sendMessageToSlack(CompletableFuture<String> message, String channelId) {
		
		message.thenAccept
		(
				response -> 
				{
					String stringToSend = response != null ? response : "[No response]";
					ChatPostMessageRequest request = ChatPostMessageRequest.builder()
	                .channel(channelId)
	                .text(stringToSend)
	                .build();
                    log.debug("sendMessageToSlack response = {}", response);
                    log.debug("stringToSend = {}", stringToSend);

			        try {
                        log.info("sendMessageToSlack: Channel= {}, Message= {}", channelId, stringToSend);
			            slackBotClient.chatPostMessage(request);
			        } 
			        catch (IOException | SlackApiException e){
                        log.error(e.getMessage());
			        }
		    }
		)
		.exceptionally(ex ->
			{
                log.error("Error in GPT response future: {}", ex.getMessage());
			       return null;
			}
		);
	}
	
	public SlackUser getSlackUserBySlackId(String slackid) {
		return jpaSlackrepo.findBySlackId(slackid);
	}
	
	public List<SlackUser> getAllSlackUsers()
	{
		List<SlackUser> users = jpaSlackrepo.findAllByOrderBySlackId();
        log.debug("All slack users: {}", users.toString());
		return users;
	}
	
	@Override
	public String registerUser(SlackUser user) {
        jpaSlackrepo.save(user);
        log.info("Registered user: {}", user);
        return SUCCESSFULL_REGISTRATION_MESSAGE  ;
    }
}
