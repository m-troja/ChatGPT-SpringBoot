package com.michal.openai.gpt.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.log.JsonSaver;
import com.michal.openai.persistence.*;
import lombok.Data;
import org.apache.http.ParseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.michal.openai.entity.GptFunction;
import com.michal.openai.entity.GptMessage;
import com.michal.openai.functions.Function;
import com.michal.openai.functions.FunctionFacory;
import com.michal.openai.entity.GptRequest;
import com.michal.openai.entity.GptResponse;
import com.michal.openai.entity.GptTool;
import com.michal.openai.entity.SlackUser;
import com.michal.openai.gpt.GptService;

import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class DefaultGptService implements GptService {

	private static final String ROLE_USER = "user";
	private static final String ROLE_SYSTEM = "system";
	private static final String ROLE_ASSISTANT = "assistant";
    @Value("${CHAT_MODEL}")
    private String model;
    @Value("${gpt.chat.temperature}")
    private Double temperature;
    @Value("${gpt.chat.presence.penalty}")
    private Double presencePenalty;
    @Value("${CHAT_MAX_TOKENS}")
    private Integer maxTokens;
    @Value("${gpt.chat.sendrequest.retryattempts}")
    private Integer retryAttempts;
    @Value("${gpt.chat.sendrequest.waitforretry.seconds}")
    private Integer waitSeconds;
    @Value("${gpt.chat.qty.context.messages}")
    private Integer qtyOfContextMessages;
    @Value("${gpt.chat.system.initial.message}")
    private String systemInitialMessage;
    @Value("${CHAT_JSON_DIR}")
    private String jsonDir;
    private List<GptTool> tools;
    private JpaGptRequestRepo jpaGptRequestRepo;
    private final RequestJdbcTemplateRepo requestTemplateRepo;
    @Qualifier("gptRestClient")
    private final RestClient restClient;
    private final FunctionFacory functionFactory;
    private final JpaGptResponseRepo jpaGptResponseRepo;
    private final JpaGptMessageRepo messageRepo;
    private final ResponseJdbcTemplateRepo responseJdbc;
    private final JpaSlackRepo jpaSlackrepo;
    private final ObjectMapper objectMapper;

    // Constructor needed because of @Qualifier("gptRestClient")
    public DefaultGptService(JpaGptRequestRepo jpaGptRequestRepo, RequestJdbcTemplateRepo requestTemplateRepo, @Qualifier("gptRestClient") RestClient restClient, FunctionFacory functionFactory, JpaGptResponseRepo jpaGptResponseRepo,
                             JpaGptMessageRepo messageRepo, ResponseJdbcTemplateRepo responseJdbc, List<GptTool> tools, JpaSlackRepo jpaSlackrepo, ObjectMapper objectMapper) {
        this.jpaGptRequestRepo = jpaGptRequestRepo;
        this.requestTemplateRepo = requestTemplateRepo;
        this.restClient = restClient;
        this.functionFactory = functionFactory;
        this.jpaGptResponseRepo = jpaGptResponseRepo;
        this.messageRepo = messageRepo;
        this.responseJdbc = responseJdbc;
        this.tools = tools;
        this.jpaSlackrepo = jpaSlackrepo;
        this.objectMapper = objectMapper;
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
			String userRealName = getSlackUserBySlackId(userSlackId).getSlackName();
			String query = queryFuture.get();
			
    		SlackUser slackUserRequestAuthor = new SlackUser(userSlackId, userRealName);

	    	gptRequest.setAuthor(userSlackId);
	    	gptRequest.setContent(query);
	    	gptRequest.setAuthorRealname(userRealName);

            log.info("getAnswerToSingleQuery : {}, userName : {}, gptFunctions : {}", query, userSlackId, gptFunctions);
	
			if (userSlackId != null) {
				userSlackId = userSlackId.replaceAll("\\s+", "_");
			}

            List<GptMessage> messages = getLastMessagesOfUserSlackId(userSlackId, gptRequest, query);

			/* Define request parameters */
			
			gptRequest.setModel(model);
			gptRequest.setTemperature(temperature);
			gptRequest.setPresencePenalty(presencePenalty);
			gptRequest.setMaxOutputTokens(maxTokens);
			gptRequest.setMessages(messages);
			gptRequest.setAuthor(slackUserRequestAuthor.getSlackUserId());
			gptRequest.setAuthorRealname(slackUserRequestAuthor.getSlackName());

	        log.info("Found {} GPT Functions", gptFunctions.length);
	        if (gptFunctions != null && gptFunctions.length > 0) {

	        	// New OpenAI API says to wrap function into tool
	        	for ( GptFunction function : gptFunctions)
	        	{
	        		tools.add(new GptTool("function", function));
	        		log.debug("Added function: {}", function.toString() );
	        	}
	        }
	        gptRequest.setTools(tools);

	        return getResponseFromGpt(gptRequest, slackUserRequestAuthor);  

    	}
    	catch (Exception e)
    	{
    		log.error("Error in getAnswerToSingleQuery");
            log.error(e.getMessage());
    	}
    	
		log.error("Returning null from getAnswerToSingleQuery");
		
		return null;
	}

	@Async("defaultExecutor")
	public CompletableFuture<String> getResponseFromGpt(GptRequest gptRequest, SlackUser slackUserRequestAuthor) throws IOException 
	{
		gptRequest.setAuthor(slackUserRequestAuthor.getSlackUserId());
		gptRequest.setAuthorRealname(slackUserRequestAuthor.getSlackName());
		saveGptRequest(gptRequest);

        log.debug("Final request to GPT: {}", gptRequest);

        GptResponse gptResponse;

		for (int i = 0; i < retryAttempts ; i++)
		{
            log.info("Calling GPT with RestClient");
			try 
			{
				gptResponse = restClient.post()
						.body(gptRequest)
						.retrieve()
		        		.body(GptResponse.class);
		        log.debug("Rest client raw response from GPT: {}", gptResponse);

				return extractGptResponseContent(gptRequest, gptResponse, slackUserRequestAuthor);
			}
			catch(RuntimeException e) 
			{
                log.error("Caught exception in getResponseFromGpt!");
				log.error(e.getMessage());
				try
				{
                    log.error("Sleep for {}s", waitSeconds);
					TimeUnit.SECONDS.sleep(waitSeconds);
                    log.error("Trying to getResponseFromGpt again...");
				}
				catch (InterruptedException e1)
				{
                    log.error(e1.getMessage());
				}
			}                                                                   
		}
		return null;
	}
	
	@Async("defaultExecutor")
	private CompletableFuture<String> extractGptResponseContent(GptRequest gptRequest, GptResponse gptResponse, SlackUser slackUserRequestAuthor) throws ParseException
	{
		log.info("Extracting gptResponseContent gptRequest:");
        logPrettyJson(gptRequest);
        log.debug("extractGptResponseContent gptResponse:");
        logPrettyJson(gptResponse);
		if (gptResponse != null)
		{
			if (gptResponse.getChoices() != null && !gptResponse.getChoices().isEmpty()) {
                log.debug("Found some choices...");
			    GptMessage message = gptResponse.getChoices().getFirst().getMessage();
                log.debug("Found message: {}", message.toString());
                if (message.getContent() != null) {
			        gptResponse.setContent(message.getContent());
			    } else {
			        log.error("GPT message content is null, cannot save response content!");
			        gptResponse.setContent("GPT message content is null.");
			    }
			} else {
			    log.debug("GPT response has no choices!");
			    gptResponse.setContent("No answer from GPT");
			}
			
			gptResponse.setRequestId(gptRequest.getId()); // Assign request_id to GptResponse (for DB column)
			log.debug("gptRequest.getId(): {}",  gptRequest.getId());
			gptResponse.setRequestSlackID(gptRequest.getAuthor()); // Assign request_author_slackid to GptResponse (later to return context of last responses)
			log.debug("gptRequest.getAuthor(): {}", gptRequest.getAuthor());
			saveResponse(gptResponse); // Store response into DB

			log.debug("Extracted gptResponse from GPT: {}", gptResponse);

			GptMessage message = gptResponse.getChoices().getFirst().getMessage(); // Extract message from response
            log.info("Extracted message from GPT: {}", message.toString());

			List<GptMessage.Tool> toolCalls = message.getToolCalls(); // Extract tools - any function calls by GPT

			if (toolCalls != null && !toolCalls.isEmpty()) // if true, executing functions
			{	
				log.debug("Found toolCalls in GPT response: {}", toolCalls);
				
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
	
	
						    log.debug("Function call arguments: {}", gptRequest);
	
						    try {
								return getResponseFromGpt(gptRequest, slackUserRequestAuthor);
							} catch (IOException e) {
								log.error(e.getMessage());
							}
							return functionResponse; 
						});
				}
			}
			 return CompletableFuture.completedFuture(message.getContent()); // executed when no function was called
		}
		return  CompletableFuture.completedFuture("");
	}
	
	public void saveGptRequest(GptRequest gptRequest)  {
        JsonSaver jsonSaver = new JsonSaver(jsonDir);
        jsonSaver.saveRequest(gptRequest);
		jpaGptRequestRepo.save(gptRequest);
	}
	
	public void saveResponse(GptResponse gptResponse) {
        try { JsonSaver jsonSaver = new JsonSaver(jsonDir);
            jsonSaver.saveResponse(gptResponse);
        }
        catch(Exception e) {
            log.error("Error saving json!");
            log.error(e.getMessage());
        }
		try{ jpaGptResponseRepo.save(gptResponse); }
        catch(Exception e) {
            log.error("Error when saving gptResponse into DB: {}", e.getMessage());
        }
	}
	
	/* Get last messages of user to build context for GPT */ 
	
	public List<GptMessage> getLastRequestsOfUser(SlackUser user)
	{
		log.info("Calling getLastMessagesOfUser with params: {} <-> {}", user.toString(), qtyOfContextMessages);
		
		List<String> messages = requestTemplateRepo.getLastRequestsBySlackId(user.getSlackUserId(), qtyOfContextMessages);
		List<GptMessage> gptMessages = new ArrayList<>();
		
		for (String message : messages)
		{
			GptMessage gptMessage = new GptMessage( ROLE_USER, message, user.getSlackUserId()  );
			log.info("Message: {}, gptMessage: {}, slackID: {}", message, gptMessage, user.getSlackUserId() );
			gptMessages.add(gptMessage);
		}
		log.debug("gptMessages.toString(): {}", gptMessages);
		return gptMessages;
	}
	
	/* Get last responses to user to build context for GPT */ 

	public List<GptMessage> getLastResponsesToUser(SlackUser user)
	{
        log.info("Calling getLastResponsesToUser with user: {}, qtyOfContextMessages: {}", user.toString(), qtyOfContextMessages);
		
		List<String> messages = responseJdbc.getLastResponsesToUser(user.getSlackUserId(), qtyOfContextMessages);
		List<GptMessage> gptMessages = new ArrayList<>();
		
		for (String message : messages)
		{
			GptMessage gptMessage = new GptMessage( ROLE_ASSISTANT, message, user.getSlackUserId()  );
            log.debug("Message: {}, gptMessage: {}", message, gptMessage);
			gptMessages.add(gptMessage);
		}

        log.info("gptMessages.toString() {}", gptMessages);
		return gptMessages;
	}

    private List<GptMessage> getLastMessagesOfUserSlackId(String userSlackId, GptRequest gptRequest, String query)
    {			/* Define list of messages to sent to GPT */

        List<GptMessage> requests = getLastRequestsOfUser(getSlackUserBySlackId(userSlackId));
        List<GptMessage> responses = getLastResponsesToUser(getSlackUserBySlackId(userSlackId));
        List<GptMessage> messages = new ArrayList<>();

        GptMessage message = new GptMessage(ROLE_USER, query, userSlackId);

        int contextSize = Math.min(requests.size(), responses.size());

        for (int i = contextSize - 1 ;  i >= 0; i--) {
            messages.add(requests.get(i));
            messages.add(responses.get(i));
        }
        messages.add(getInitialSystemMessage(gptRequest.getAuthor()));
        messages.add(message);

        log.info("added total {} messages into context", messages.size() );
        return messages;
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
    private void logPrettyJson(Object obj) {
        try {
            String prettyJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
            log.debug(prettyJson);
        } catch (Exception e) {
            log.error("Could not create pretty print JSON", e);
        }
    }
    public SlackUser getSlackUserBySlackId(String slackId) {
        return jpaSlackrepo.findBySlackUserId(slackId);
    }
	
}