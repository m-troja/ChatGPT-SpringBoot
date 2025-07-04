package com.michal.openai.slack.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
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

@Slf4j
@Service
public class DefaultSlackService implements SlackService {

	@Autowired
	@Qualifier("slackBotClient")
	private MethodsClient slackBotClient;
	public static final String SUCCESSFULL_REGISTRATION_MESSAGE = "User is registered!";
	private static final String REGISTRATION_ERROR_MESSAGE = "Error - user already registered!.";

	@Autowired
	private List<GptFunction> functions;
	
	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	JpaSlackRepo jpaSlackrepo;

    private GptService gptService;

    @Autowired
    public void setGptService(@Lazy GptService gptService) {
        this.gptService = gptService;
    }
	
	@Async("defaultExecutor")
	@Override
	public void processOnMentionEvent(String requestBody) {
		SlackRequestData slackRequestData = extractSlackRequestData(requestBody);
		log.debug("processOnMentionEvent requestBody : " + requestBody);

		// String gptResponseString = gptService.getAnswerToSingleQuery(slackRequestData.getMessage(), slackRequestData.getMessageAuthorId());
		CompletableFuture<String> gptResponseString = gptService.getAnswerToSingleQuery(CompletableFuture.completedFuture(slackRequestData.getMessage()), CompletableFuture.completedFuture( slackRequestData.getMessageAuthorId() ) ,functions.toArray(GptFunction[]::new));

		log.info("call sendMessageToSlack: " + gptResponseString+", channel: " + slackRequestData.getChannelIdFrom());
		
		System.out.println("processOnMentionEvent channel: " + slackRequestData.getChannelIdFrom());
		sendMessageToSlack(gptResponseString, slackRequestData.getChannelIdFrom());
	//	sendMessageToSlack(gptResponseString, "C08RLDBCRB9");
	}
	
	private SlackRequestData extractSlackRequestData(String requestBody) {
		log.info("extractSlackRequestData....");
		JsonNode jsonNode = null;
		
		try {
			jsonNode = objectMapper.readValue(requestBody, JsonNode.class);
		}
		catch(Exception e)
		{
			log.info("Error in jsonObject = objectMapper.readValue(requestBody, JsonObject.class)!");
			e.printStackTrace();
		}

        // Extract the 'event' object
        JsonNode eventNode = null;
		try {
			eventNode = jsonNode.path("event");
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        // Extract the fields from the 'event' object
        String messageAuthorId = eventNode.path("user").asText();
        String message = eventNode.path("text").asText();
        String channelIdFrom = eventNode.path("channel").asText();
		SlackUser user = new SlackUser();
		try
		{
			// Use a Map to before sending request to Slack API
			if ( getSlackUserBySlackId(messageAuthorId) == null)
			{
				log.info(" messageAuthorId not found! Extracting users...");
				extractUsersIntoDatabase();
				
				 user = getSlackUserBySlackId(messageAuthorId);
				
				log.info("slackuser extracted :  " + user.toString());
			}
		}
		catch( Exception e)
		{
			
		}
		String messageWithNames = substituteUserIdsWithUserNames(message);
		
		log.info("extractSlackRequestData: " + messageAuthorId + " : "+messageWithNames + " : "+channelIdFrom);

		return new SlackRequestData(messageAuthorId, messageWithNames, channelIdFrom);
	}

	private String substituteUserIdsWithUserNames(String message) {
		
		// Extract all slackIDs in a message to string array
		String[] userIds = extractUserIds(message);
		
		StringBuilder stringBuilder = new StringBuilder(message);
		log.info("stringBuilder : " + stringBuilder.toString() );

		// For slackID in array of slackIDs, assign realName and switch slackID to realName
		for(String userId : userIds)
		{
			String name = getSlackUserBySlackId(userId).getRealName();
			String mention = "<@" + userId + ">";
			
			if(name != null)
			{
				int startIndex = stringBuilder.indexOf(mention);
				int endIndex = startIndex + mention.length();
				log.info("startIndex : " + startIndex, ", endIndex : " + endIndex );

				stringBuilder.replace(startIndex, endIndex, name);
			}
		}
		log.info("stringBuilder : " + stringBuilder.toString() );

		return stringBuilder.toString();
	}

	private String[] extractUserIds(String message) {
		
		List<String> userIds = new ArrayList<>();
		Pattern pattern = Pattern.compile("<@(\\w+)>");
		Matcher matcher = pattern.matcher(message);
		while (matcher.find()) {
			userIds.add(matcher.group(1));
		}
		log.info("extractUserIds : " + userIds.toArray().toString() );

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
						log.info("registerUser : " + user.getId() + " : " + user.getRealName() );
					}
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
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
					log.info("Response = " + response);
					log.info("stringToSend = " + stringToSend);

			        try 
			        {
			        	log.debug("sendMessageToSlack: Channel= " + channelId + ", Message= " + stringToSend);
			            slackBotClient.chatPostMessage(request);
			        } 
			        catch (IOException | SlackApiException e) 
			        {
			            e.printStackTrace();
			        }
		    }
		)
		.exceptionally
		(ex -> 
			{
				log.error("Error in GPT response future: " + ex.getMessage());
			       ex.printStackTrace();
			       return null;
			}
		);
	}
	
	public SlackUser getSlackUserBySlackId(String slackid)
	{
		return jpaSlackrepo.findBySlackId(slackid);
	}
	
	public List<SlackUser> getAllSlackUsers()
	{
		List<SlackUser> users = jpaSlackrepo.findAllByOrderBySlackId();
		return users;
	}
	
	@Override
	public String registerUser(SlackUser user) {
		if ( jpaSlackrepo.save(user) != null)
		{
			log.info(SUCCESSFULL_REGISTRATION_MESSAGE);
			return SUCCESSFULL_REGISTRATION_MESSAGE  ;
		}
		else 
		{
			log.info(REGISTRATION_ERROR_MESSAGE);
			return REGISTRATION_ERROR_MESSAGE;
		}
	}

}
