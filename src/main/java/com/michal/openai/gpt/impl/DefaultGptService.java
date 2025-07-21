package com.michal.openai.gpt.impl;

import java.io.IOException;
import java.util.ArrayList;
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
import com.michal.openai.entity.SlackUser;
import com.michal.openai.gpt.GptService;
import com.michal.openai.log.JsonSaver;
import com.michal.openai.persistence.JpaGptMessageRepo;
import com.michal.openai.persistence.JpaGptRequestRepo;
import com.michal.openai.persistence.JpaGptResponseRepo;
import com.michal.openai.persistence.RequestJdbcTemplateRepo;
import com.michal.openai.persistence.ResponseJdbcTemplateRepo;
import com.michal.openai.slack.SlackService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DefaultGptService implements GptService {
	
	private static final String ROLE_USER = "user";
	private static final String ROLE_SYSTEM = "system";
	private static final String ROLE_ASSISTANT = "assistant";
	
	@Autowired
	JpaGptRequestRepo jpaGptRequestRepo;
	
	@Autowired
	RequestJdbcTemplateRepo requestTemplateRepo;
	
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
	
	@Value("${gpt.chat.qty.context.messages}")
	private int qtyOfContextMessages;
	
	@Value("${gpt.chat.system.initial.message}")
	private String systemInitialMessage;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	HttpClient httpClient;

	@Autowired
	FunctionFacory functionFactory;
		
	@Autowired
	JpaGptResponseRepo jpaGptResponseRepo;
	
	@Autowired
	JpaGptMessageRepo meesageRepo;
	
	@Autowired
	ResponseJdbcTemplateRepo responseJdbc;
	
	List<GptTool> tools = new ArrayList<>();

	private SlackService slackService;

	@Autowired
	public void setSlackService(SlackService slackService) {
	    this.slackService = slackService;
	}
	
	/*
	 * Called by SlackAPI controller.
	 * Builds object of GPTRequest and forwards it to send to GPT.
	 */
	@Override
	public CompletableFuture<String> getAnswerToSingleQuery(CompletableFuture<String> queryFuture, CompletableFuture<String> userNameFuture, GptFunction... gptFunctions ) {
		GptRequest gptRequest = new GptRequest();
    	try {
    	
			String userSlackId = userNameFuture.get();
			String userRealName = slackService.getSlackUserBySlackId(userSlackId).getRealName();
			String query = queryFuture.get();
			
    		SlackUser slackUserRequestAuthor = new SlackUser(userSlackId, userRealName);

	    	gptRequest.setAuthor(userSlackId);
	    	gptRequest.setContent(query);
	    	gptRequest.setAuthorRealname(userRealName);
	    
			log.info("getAnswerToSingleQuery : " + query + ", userName : " + userSlackId + ", gptFunctions : " + gptFunctions.toString());
	
			if (userSlackId != null) {
				userSlackId = userSlackId.replaceAll("\\s+", "_");
			}
			
			/* Define list of messages to sent to GPT */ 
			
			List<GptMessage> requests = getLastRequestsOfUser(slackService.getSlackUserBySlackId(userSlackId));
			List<GptMessage> responses = getLastResponsesToUser(slackService.getSlackUserBySlackId(userSlackId));
			List<GptMessage> messages = new ArrayList<>();	
			
			GptMessage message = new GptMessage(ROLE_USER, query, userSlackId);
			
			int contextSize = Math.min(requests.size(), responses.size());
			
			for (int i = contextSize - 1 ;  i >= 0; i--) {
				messages.add(requests.get(i));
				messages.add(responses.get(i));
			}
			
			messages.add(getInitialSystemMessage(gptRequest.getAuthor()));
			messages.add(message);
			
			/* Define request parameters */
			
			gptRequest.setModel(model);
			gptRequest.setTemperature(temperature);;
			gptRequest.setPresencePenalty(presencePenalty);
			gptRequest.setMaxTokens(maxTokens);
			gptRequest.setMessages(messages);
			gptRequest.setAuthor(slackUserRequestAuthor.getSlackId());
			gptRequest.setAuthorRealname(slackUserRequestAuthor.getRealName());
			saveGptRequest(gptRequest);

	        log.debug("saveGptRequest : " + gptRequest.toString() );

	        if (gptFunctions != null && gptFunctions.length > 0) {
	        	
	        	// New OpenAI API says to wrap function into tool
	        	for ( GptFunction function : gptFunctions)
	        	{
	        		
	        		tools.add(new GptTool("function", function));
	        		log.debug("function : " + function.toString() );
	        	}
	        }
	        
	        gptRequest.setTools(tools);
	        
	        log.debug("gptRequest : " + gptRequest.toString() );

	        return getResponseFromGpt(gptRequest, slackUserRequestAuthor);  

    	}
    	catch (Exception e)
    	{
    		log.error("Error in getAnswerToSingleQuery");
    		e.printStackTrace();
    	}
    	
		log.error("Returning null from getAnswerToSingleQuery");
		
		return null;
	}	
	
	/* When userName is not known */
	
	@Async("defaultExecutor")
	public CompletableFuture<String> getResponseFromGpt(GptRequest gptRequest, SlackUser slackUserRequestAuthor) 
	{
		gptRequest.setAuthor(slackUserRequestAuthor.getSlackId());
		gptRequest.setAuthorRealname(slackUserRequestAuthor.getRealName());
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
			log.error("Error saving response json!");
			e.printStackTrace();
		}
		
		for (int i = 0; i < retryAttempts ; i++)
		{
			try 
			{
				response = extractGptResponseContent(postRequest, gptRequest, slackUserRequestAuthor);
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
	private CompletableFuture<String> extractGptResponseContent(HttpPost postRequest, GptRequest gptRequest, SlackUser slackUserRequestAuthor) throws ParseException, IOException  
	{
		HttpResponse response = httpClient.execute(postRequest);
		int statusCode = response.getStatusLine().getStatusCode();
		HttpEntity entity = response.getEntity();
		
		
		if (entity != null) // Return status BAD_REQUEST when sending POST request failed.
		{
			String responseBody = EntityUtils.toString(entity);
			if (statusCode == HttpStatus.SC_BAD_REQUEST)
			{
		        log.error("Error sending POST request to ChatGPT!");

				return CompletableFuture.completedFuture(responseBody);
			}

			GptResponse gptResponse = objectMapper.readValue(responseBody, GptResponse.class); // Parse response JSON to GptResponse object
			
			if (gptResponse.getChoices() != null && !gptResponse.getChoices().isEmpty()) {
			    GptMessage message = gptResponse.getChoices().get(0).getMessage();																																																																																																																																																	if (message != null && message.getContent() != null) {
			        gptResponse.setContent(message.getContent());
			    } else {
			        log.info("GPT message content is null, cannot save response content!");
			        gptResponse.setContent("GPT message content is null.");
			    }
			} else {
			    log.info("GPT response has no choices!");
			    gptResponse.setContent("No answer from GPT");
			}
			
			gptResponse.setRequestId(gptRequest.getId()); // Assign requst_id to GptResponse (for DB column)
			log.debug("gptRequest.getId() " + gptRequest.getId());
			gptResponse.setRequestSlackID(gptRequest.getAuthor()); // Assign request_author_slackid to GptResponse (later to return context of last responses)
			log.debug("gptRequest.getAuthor() " + gptRequest.getAuthor());
			saveResponse(gptResponse); // Store response into DB

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

			
			GptMessage message = gptResponse.getChoices().get(0).getMessage(); // Extract message from response
			List<GptMessage.Tool> toolCalls = message.getToolCalls(); // Extract tools - any function calls by GPT

			if (toolCalls != null && toolCalls.size() != 0)
			{	
				log.info("message by GPT = " + message.toString());
				log.info("toolCalls by GPT " + toolCalls.toString() );
				
				for ( GptMessage.Tool tool : toolCalls) // If there is any tool (function) to be used
				{
					GptMessage.Tool.FunctionCall functionCall = tool.getFunctionCall(); // Get object of function_call
					
					Function function = functionFactory.getFunctionByFunctionName(functionCall.getName()); // Get name of function
					
					log.info("Calling function '{}' with arguments: {}", functionCall.getName(), functionCall.getArguments());

					CompletableFuture<String> functionResponse = function.execute(functionCall.getArguments()); // Execute function and store response as string 
					
					/*
					 *  Because function was executed, we need to return result of function to GPT, so GPT answers to user
					 */
					return functionResponse.thenCompose(funcResp -> {  
					    GptMessage gptMessage = new GptMessage();
					    gptMessage.setRole("function");
					    gptMessage.setContent(funcResp); 
					    gptMessage.setName(functionCall.getName());
					    tool.setFunctionCall(functionCall);
					    gptRequest.getMessages().add(gptMessage);
					    gptRequest.setFunctions(null);


					    log.debug("Function call arguments : " + gptRequest.toString() );

					    return getResponseFromGpt(gptRequest, slackUserRequestAuthor); 
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
			log.error("Error in objectMapper.writeValueAsString(gptRequest)!");
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
	
	/* Get last messages of user to build context for GPT */ 
	
	public List<GptMessage> getLastRequestsOfUser(SlackUser user)
	{
		log.info("Calling getLastMessagesOfUser with params  : {} : {}", user.toString(), qtyOfContextMessages);
		
		List<String> messages = requestTemplateRepo.getLastRequestsBySlackId(user.getSlackId(), qtyOfContextMessages);
		List<GptMessage> gptMessages = new ArrayList<>();
		
		for (String message : messages)
		{
			GptMessage gptMessage = new GptMessage( ROLE_USER, message, user.getSlackId()  );
			log.info("meesage : " + message + ", gptMessage : " + gptMessage.toString() );
			gptMessages.add(gptMessage);
		}
		
		log.info("gptMessages.toString() " + gptMessages.toString());
		
		return gptMessages;
	}
	
	/* Get last responses to user to build context for GPT */ 

	public List<GptMessage> getLastResponsesToUser(SlackUser user)
	{
		log.info("Calling getLastResponsesToUser with params  : " +  user.toString(), " : " + qtyOfContextMessages );
		
		List<String> messages = responseJdbc.getLastResponsesToUser(user.getSlackId(), qtyOfContextMessages);
		List<GptMessage> gptMessages = new ArrayList<>();
		
		for (String message : messages)
		{
			GptMessage gptMessage = new GptMessage( ROLE_ASSISTANT, message, user.getSlackId()  );
			log.info("meesage : " + message + ", gptMessage : " + gptMessage.toString() );
			gptMessages.add(gptMessage);
		}
		
		log.info("gptMessages.toString() " + gptMessages.toString());
		
		return gptMessages;
	}
	
	public GptMessage getInitialSystemMessage(String userSlackId) 
	{
		String content = String.format(
			    "%sYou received message from %s. Type <@%s> to mention them.",
			    systemInitialMessage,
			    userSlackId,
			    userSlackId
			);
			return new GptMessage(ROLE_SYSTEM, content);
	}
}