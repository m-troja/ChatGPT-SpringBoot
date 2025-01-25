package com.michal.openai.slack.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.michal.openai.entity.SlackRequestData;
import com.michal.openai.gpt.GptService;
import com.michal.openai.slack.SlackService;
import com.slack.api.model.User;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.users.UsersListRequest;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.block.element.RichTextSectionElement.Text;

@Service
public class DefaultSlackService implements SlackService {

	@Autowired
	@Qualifier("slackBotClient")
	private MethodsClient slackBotClient;
	
	@Autowired
	Gson gson;
	
	@Autowired
	private GptService gptService;
	
	@Override
	public void processOnMentionEven(String requestBody) {
		SlackRequestData slackRequestData = extractSlackRequestData(requestBody);
		
		String gptResponseString = gptService.getAnswerToSingleQuery(slackRequestData.getMessage(), slackRequestData.getMessageAuthorId());
		
		System.out.println("sendMessageToSlack: " + gptResponseString+":"+ slackRequestData.getChannelIdFrom());
		sendMessageToSlack(gptResponseString, slackRequestData.getChannelIdFrom());
		
		
	}
	
	private SlackRequestData extractSlackRequestData(String requestBody) {
		JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
		JsonObject eventJsonObject = jsonObject.getAsJsonObject("event");
		
		String messageAuthorId = eventJsonObject.get("user").getAsString();
		String message = eventJsonObject.get("text").getAsString();
		String channelIdFrom = eventJsonObject.get("channel").getAsString();
		
		Map<String, String> allUserIdToUserMap = extractUserIdToNameMap();
		String messageWithNames = substituteUserIdsWithUserNames(message, allUserIdToUserMap);
		
		System.out.println("extractSlackRequestData: " + messageAuthorId + ":"+messageWithNames+":"+channelIdFrom);
		// TODO
		// extractSlackRequestData: U088ARD1VEG:null:C08887LK9KM

		return new SlackRequestData(messageAuthorId, messageWithNames, channelIdFrom);
	}

	private String substituteUserIdsWithUserNames(String message, Map<String, String> allUserIdToUserMap) {
		String[] userIds = extractUserIds(message);
		StringBuilder stringBuilder = new StringBuilder(message);
		
		for(String userId : userIds)
		{
			String name = allUserIdToUserMap.get(userId);
			String mention = "<@" + userId + ">";
			
			if(name != null)
			{
				int startIndex = stringBuilder.indexOf(mention);
				int endIndex = startIndex + mention.length();
				stringBuilder.replace(startIndex, endIndex, name);
			}
		}
		return stringBuilder.toString();
	}

	private String[] extractUserIds(String message) {
		
		List<String> userIds = new ArrayList<>();
		Pattern pattern = Pattern.compile("<@(\\w+)>");
		Matcher matcher = pattern.matcher(message);
		while (matcher.find()) {
			userIds.add(matcher.group(1));
		}
		return userIds.toArray(new String[userIds.size()]);
	}

	private Map<String, String> extractUserIdToNameMap() {
			try 
			{
				UsersListRequest usersListRequest = UsersListRequest.builder().build();
				UsersListResponse userListResponse = slackBotClient.usersList(usersListRequest);          
				Map <String, String> resultMap = new HashMap<>();
				for (User user : userListResponse.getMembers())
				{
					System.out.println("extractUserIdToNameMap: " + user.getId() + ":" + user.getRealName() );
					resultMap.put(user.getId(), user.getRealName());
				}
				return resultMap;
			} 
			catch (IOException | SlackApiException e) 
			{
				e.printStackTrace();
			}
		
		return null;
	}

	public void sendMessageToSlack(String responseToSlack, String channelId)
	{
		ChatPostMessageRequest request = ChatPostMessageRequest.builder().channel(channelId).text(responseToSlack).build();
	
		try 
		{
			System.out.println("try sendMessageToSlack: Team= " + channelId +  ", Message= "+ responseToSlack);

			slackBotClient.chatPostMessage(request);
		}
		catch (IOException | SlackApiException e)
		{
			e.printStackTrace();
		}
	}
}
