package com.michal.openai.gpt.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.GptFunction;
import com.michal.openai.entity.GptMessage;
import com.michal.openai.functions.Function;
import com.michal.openai.functions.FunctionFacory;
import com.michal.openai.entity.GptRequest;
import com.michal.openai.entity.GptResponse;
import com.michal.openai.entity.GptTool;
import com.michal.openai.gpt.GptService;
import com.michal.openai.log.JsonSaver;
import com.michal.openai.persistence.JpaGptRequestRepo;
import com.michal.openai.persistence.JpaGptResponseRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DefaultGptService implements GptService {
	
	private static final String ROLE_USER = "user";
	
	@Autowired
	JpaGptRequestRepo jpaGptRequestRepo;
	
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
	private ObjectMapper objectMapper;
	
	@Autowired
	HttpClient httpClient;

	@Autowired
	FunctionFacory functionFactory;
		
	@Autowired
	JpaGptResponseRepo jpaGptResponseRepo;
	
	List<GptTool> tools = new ArrayList<>();
	
	private int i;
	
	@Async("defaultExecutor")
	@Override
	public CompletableFuture<String> getAnswerToSingleQuery(CompletableFuture<String> query, GptFunction... gptFunctions) {
	    return query.thenCompose(unwrappedQuery -> {
	    	
	    	log.debug("getAnswerToSingleQuery : " + query + ", gptFunctions : " + gptFunctions.toString());
	    	
	    	GptRequest gptRequest = new GptRequest();
	        List<GptMessage> messages = new ArrayList<>();
	        GptMessage message = new GptMessage(ROLE_USER, unwrappedQuery);  // ✅ now a String
	        messages.add(message);

	        gptRequest.setModel(model);
	        gptRequest.setTemperature(temperature);
	        gptRequest.setPresencePenalty(presencePenalty);
	        gptRequest.setMaxTokens(maxTokens);
	        gptRequest.setMessages(messages);
	        gptRequest.setToolChoice("auto");
	        
	        log.debug("gptRequest : " + gptRequest.toString() );
	        
	        if (gptFunctions != null && gptFunctions.length > 0) {
	        	
	        	// New OpenAI API says to wrap function into tool
	        	for ( GptFunction function : gptFunctions)
	        	{
	        		tools.add(new GptTool("function", function));
	        		log.debug("function : " + function.toString() );
	        	}
	        
	        }
	        gptRequest.setTools(tools);
	        
	        return getResponseFromGpt(gptRequest);  // returns CompletableFuture<String>
	    });
	}
	
	@Override
	public CompletableFuture<String> getAnswerToSingleQuery(String query, String userName, GptFunction... gptFunctions) {
		GptRequest gptRequest = new GptRequest();
		
    	log.info("getAnswerToSingleQuery : " + query + ", userName : " + userName + ", gptFunctions : " + gptFunctions.toString());
		
		if (userName != null) {
			userName = userName.replaceAll("\\s+", "_");
		}
		
		List<GptMessage> messages = new ArrayList<>();
		messages.add( new GptMessage(ROLE_USER, query, userName) );
		gptRequest.setModel(model);
		gptRequest.setTemperature(temperature);;
		gptRequest.setPresencePenalty(presencePenalty);
		gptRequest.setMaxTokens(maxTokens);
		gptRequest.setMessages(messages);
		
        log.debug("gptRequest : " + gptRequest.toString() );

		if ( gptFunctions != null && gptFunctions.length > 0)
		{
			List<GptFunction> functions = Arrays.asList(gptFunctions);
			gptRequest.setFunctions(functions);
		}
		
		return getResponseFromGpt(gptRequest);
	}
	
	
	@Async("defaultExecutor")
	public CompletableFuture<String> getResponseFromGpt(GptRequest gptRequest) 
	{
		saveGptRequest(gptRequest);
		
        log.debug("saveGptRequest : " + gptRequest.toString() );

		HttpPost postRequest = prepareHttpPostRequest(gptRequest);
		
		CompletableFuture<String> response = CompletableFuture.completedFuture("");
		
		// Save request to JSON
		JsonSaver jsonSaver = new JsonSaver();
		try {
			String requestBody = objectMapper.writeValueAsString(gptRequest);
			jsonSaver.saveGptRequestToJson(requestBody);
		}
		catch(Exception e)
		{
			log.info("Error saving json!");
			e.printStackTrace();
		}
		
		for (i = 0; i < retryAttempts ; i++)
		{
			try 
			{
				response = extractGptResponseContent(postRequest, gptRequest);
		        log.debug("response : " + gptRequest.toString() );

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
	
	@Async("defaultExecutor")
	private CompletableFuture<String> extractGptResponseContent(HttpPost postRequest, GptRequest gptRequest) throws ParseException, IOException  
	{
		HttpResponse response = httpClient.execute(postRequest);
		int statusCode = response.getStatusLine().getStatusCode();
		HttpEntity entity = response.getEntity();
		
		
		if (entity != null)
		{
			
			String responseBody = EntityUtils.toString(entity);
			if (statusCode == HttpStatus.SC_BAD_REQUEST)
			{
				return CompletableFuture.completedFuture(responseBody);
			}

			GptResponse gptResponse = objectMapper.readValue(responseBody, GptResponse.class);
			
			if (gptResponse.getChoices() != null && !gptResponse.getChoices().isEmpty()) {
			    GptMessage message = gptResponse.getChoices().get(0).getMessage();
			    if (message != null && message.getContent() != null) {
			        gptResponse.setContent(message.getContent());
			    } else {
			        log.info("GPT message or content is null, cannot save response content!");
			        gptResponse.setContent("Brak treści w odpowiedzi GPT.");
			    }
			} else {
			    log.info("GPT response has no choices!");
			    gptResponse.setContent("Brak odpowiedzi od GPT.");
			}
			saveResponse(gptResponse);

			// Save response to JSON
			try {
				JsonSaver jsonSaver = new JsonSaver();
				jsonSaver.saveResponseJson(responseBody);
			}
			catch(Exception e)
			{
				log.info("Error saving json!");
				e.printStackTrace();
			}
			
			log.debug("extractGptResponseContent responseBody: "  + responseBody);

			GptMessage message = gptResponse.getChoices().get(0).getMessage();
			List<GptMessage.Tool> toolCalls = message.getToolCalls();

			if (toolCalls != null && toolCalls.size() != 0)
			{
				
				log.info("message = " + message.toString());
				log.info("toolCalls  " + toolCalls.toString() );
				
				for ( GptMessage.Tool tool : toolCalls)
				{
					GptMessage.Tool.FunctionCall functionCall = tool.getFunctionCall();
					
					Function function = functionFactory.getFunctionByFunctionName(functionCall.getName());
					
					log.info("functionCall = " + functionCall.toString());
					
					log.info(" =========== functionCall -> "+ functionCall.getName() + " ==========");
					log.info("extractGptResponseContent, Function call arguments: " + functionCall.getArguments());

					CompletableFuture<String> functionResponse = function.execute(functionCall.getArguments());
					
					return functionResponse.thenCompose(funcResp -> {
					    GptMessage gptMessage = new GptMessage();
					    gptMessage.setRole("function");
					    gptMessage.setContent(funcResp); 
					    gptMessage.setName(functionCall.getName());
					    tool.setFunctionCall(functionCall);
					    gptRequest.getMessages().add(gptMessage);
					    gptRequest.setFunctions(null);

					    log.debug("Function call arguments : " + gptRequest.toString() );

					    return getResponseFromGpt(gptRequest); 
					});
				}
			}
			
			 return CompletableFuture.completedFuture(message.getContent());

		}
		return  CompletableFuture.completedFuture("");
	}
	
	public HttpPost prepareHttpPostRequest(GptRequest gptRequest)
	{
		String authenticationHeader = "Bearer " + chatGptApiKey;
		String contentTypeHeader = "application/json";
		String requestBody = "";
		try {
			requestBody = objectMapper.writeValueAsString(gptRequest);
		}
		catch(Exception e)
		{
			log.info("Error in objectMapper.writeValueAsString(gptRequest)!");
			e.printStackTrace();
		}
		
		StringEntity stringEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

	    log.debug("stringEntity : " + stringEntity.toString() );

		HttpPost postRequest = new HttpPost(chatGptApiUrl);
		postRequest.setHeader(HttpHeaders.AUTHORIZATION, authenticationHeader);
		postRequest.setHeader(HttpHeaders.CONTENT_TYPE, contentTypeHeader);
		postRequest.setEntity(stringEntity);
		
	    log.debug("postRequest : " + postRequest.toString() );

		return postRequest;
	}
	
	public void saveGptRequest(GptRequest request) 
	{
		jpaGptRequestRepo.save(request);
	}
	
	public void saveResponse(GptResponse gptResponse)
	{
		jpaGptResponseRepo.save(gptResponse);
	}

}
