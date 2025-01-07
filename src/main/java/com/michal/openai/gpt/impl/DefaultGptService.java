package com.michal.openai.gpt.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.michal.openai.entity.GptMessage;
import com.michal.openai.entity.GptRequest;
import com.michal.openai.entity.GptResponse;
import com.michal.openai.gpt.GptService;

@Service
public class DefaultGptService implements GptService{

	@Value("${gpt.chat.model}")
	private String model;

	@Value("${gpt.chat.temperature}")
	private Double temperature;
	
	@Value("${gpt.chat.presence.penalty}")
	private Double presencePenalty;
	
	@Value("${gpt.chat.completion.maxtokens}")
	private Integer maxTokens;
	
	@Value("${gpt.chat.api.url}")
	private String chatGptApiUrl;
	
	@Value("${gpt.chat.sendrequest.retryattempts}")
	private Integer retryAttempts;
	
	@Value("${gpt.chat.sendrequest.waitforretry.seconds}")
	private Integer waitSeconds;
	
	@Value("${gpt.chat.api.key}")
	private String chatGptApiKey;
	
	@Autowired
	private Gson gson;
	
	@Autowired
	HttpClient httpClient;

	private int i;
	
	@Override
	public String getAnswerToSingleQuery(String query) {
		GptRequest req = new GptRequest();
		List<GptMessage> messages = new ArrayList<>();
		GptMessage message = new GptMessage("user", query);
		messages.add(message);
		req.setModel(model);
		req.setTemperature(temperature);;
		req.setPresencePenalty(presencePenalty);
		req.setMaxTokens(maxTokens);
		req.setMessages(messages);
		
		String requestBody = gson.toJson(req);
		System.out.println(requestBody);
		return getResponseFromGpt(requestBody);
	}
	
	public String getResponseFromGpt(String requestBody) 
	{
		String authenticationHeader = "Bearer " + chatGptApiKey;
		String contentTypeHeader = "application/json";
		StringEntity stringEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

		HttpPost postRequest = new HttpPost(chatGptApiUrl);
		postRequest.setHeader(HttpHeaders.AUTHORIZATION, authenticationHeader);
		postRequest.setHeader(HttpHeaders.CONTENT_TYPE, contentTypeHeader);
		
		postRequest.setEntity(stringEntity);
		
		String response = "";
		
		for (i = 0; i < retryAttempts ; i++)
		{
			try 
			{
				response = extractGptResponseContent(postRequest);
				return response;
			}
			catch(IOException | RuntimeException e) 
			{
				e.printStackTrace();
				try 
				{
					TimeUnit.SECONDS.sleep(waitSeconds);
				}
				catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
			}                                                                   
		}
		return response;
	}
	
	private String extractGptResponseContent(HttpPost postRequest) throws ClientProtocolException, IOException 
	{
		HttpResponse response = httpClient.execute(postRequest);
		int statusCode = response.getStatusLine().getStatusCode();
		HttpEntity entity = response.getEntity();
		
		if (entity != null)
		{
			String responseBody = EntityUtils.toString(entity);
			if (statusCode == HttpStatus.SC_BAD_REQUEST)
			{
				return responseBody;
			}
			GptResponse gptResponse = gson.fromJson(responseBody, GptResponse.class);
		return gptResponse.getChoices().get(0).getMessage().getContent();
		}
		
		
		return "";
	}
	

}
