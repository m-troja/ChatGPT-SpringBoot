package com.michal.openai.gpt.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
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
import com.michal.openai.entity.GptFunction;
import com.michal.openai.entity.GptMessage;
import com.michal.openai.entity.GptMessage.FunctionCall;
import com.michal.openai.functions.Function;
import com.michal.openai.functions.FunctionFacory;
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

	@Autowired
	FunctionFacory functionFactory;
	
	private int i;
	
	@Override
	public String getAnswerToSingleQuery(String query, GptFunction... gptFunctions) {
		GptRequest gptRequest = new GptRequest();
		List<GptMessage> messages = new ArrayList<>();
		GptMessage message = new GptMessage("user", query);
		messages.add(message);
		gptRequest.setModel(model);
		gptRequest.setTemperature(temperature);;
		gptRequest.setPresencePenalty(presencePenalty);
		gptRequest.setMaxTokens(maxTokens);
		gptRequest.setMessages(messages);
		
		if ( gptFunctions != null && gptFunctions.length > 0)
		{
			List<GptFunction> functions = Arrays.asList(gptFunctions);
			gptRequest.setFunctions(functions);
		}
		return getResponseFromGpt(gptRequest);
	}
	
	public String getResponseFromGpt(GptRequest gptRequest) 
	{
		HttpPost postRequest = prepareHttpPostRequest(gptRequest);
		
		String response = "";
		
		for (i = 0; i < retryAttempts ; i++)
		{
			try 
			{
				response = extractGptResponseContent(postRequest, gptRequest);
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
	
	private String extractGptResponseContent(HttpPost postRequest, GptRequest gptRequest) throws ClientProtocolException, IOException 
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
			
			GptMessage message = gptResponse.getChoices().get(0).getMessage();
			
			FunctionCall functionCall = message.getFunctionCall();
			
			if (functionCall != null)
			{
				Function function = functionFactory.getFunctionByFunctionName(functionCall.getName());
				String functionResponse = function.execute(functionCall.getArguments());
				
				GptMessage gptMessage = new GptMessage();
				gptMessage.setRole("function");
				gptMessage.setContent(functionResponse);
				gptMessage.setName(functionCall.getName());
				gptRequest.getMessages().add(gptMessage);
				gptRequest.setFunctions(null);
			
				return getResponseFromGpt(gptRequest);
			}
			
			return message.getContent();
			
		}
		
		return "";

	}
	
	public HttpPost prepareHttpPostRequest(GptRequest gptRequest)
	{
		String authenticationHeader = "Bearer " + chatGptApiKey;
		String contentTypeHeader = "application/json";
		String requestBody = gson.toJson(gptRequest);
		StringEntity stringEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

		HttpPost postRequest = new HttpPost(chatGptApiUrl);
		postRequest.setHeader(HttpHeaders.AUTHORIZATION, authenticationHeader);
		postRequest.setHeader(HttpHeaders.CONTENT_TYPE, contentTypeHeader);
		postRequest.setEntity(stringEntity);
		
		return postRequest;
	}
	

}
