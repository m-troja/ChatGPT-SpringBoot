package com.michal.openai.gpt.impl;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.GithubBranch;
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
	
	public DefaultGptService(JpaGptRequestRepo jpaGptRequestRepo, RequestJdbcTemplateRepo requestTemplateRepo,
			@Qualifier("gptRestClient") RestClient restClient, ObjectMapper objectMapper, HttpClient httpClient, FunctionFacory functionFactory,
			JpaGptResponseRepo jpaGptResponseRepo, JpaGptMessageRepo meesageRepo,
			ResponseJdbcTemplateRepo responseJdbc) {
		this.jpaGptRequestRepo = jpaGptRequestRepo;
		this.requestTemplateRepo = requestTemplateRepo;
		this.restClient = restClient;
		this.httpClient = httpClient;
		this.functionFactory = functionFactory;
		this.jpaGptResponseRepo = jpaGptResponseRepo;
		this.meesageRepo = meesageRepo;
		this.responseJdbc = responseJdbc;
	}

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
	
	@Value("${gpt.chat.sendrequest.retryattempts}")
	private Integer retryAttempts;
	
	@Value("${gpt.chat.sendrequest.waitforretry.seconds}")
	private Integer waitSeconds;
	
	@Value("${gpt.chat.qty.context.messages}")
	private int qtyOfContextMessages;
	
	@Value("${gpt.chat.system.initial.message}")
	private String systemInitialMessage;
	
	RestClient restClient;
	
	@Autowired
	HttpClient httpClient;

	FunctionFacory functionFactory;
		
	JpaGptResponseRepo jpaGptResponseRepo;
	
	JpaGptMessageRepo meesageRepo;
	
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
	public CompletableFuture<String> getAnswerToSingleQuery(
	        CompletableFuture<String> queryFuture,
	        CompletableFuture<String> userNameFuture,
	        GptFunction... gptFunctions) {

	    try {
	        String userSlackId = userNameFuture.get();
	        String query = queryFuture.get();

	        if (userSlackId == null || query == null) {
	            log.error("User Slack ID or query is null");
	            return CompletableFuture.completedFuture(null);
	        }

	        SlackUser slackUser = slackService.getSlackUserBySlackId(userSlackId);
	        if (slackUser == null) {
	            log.error("SlackUser not found for ID: {}", userSlackId);
	            return CompletableFuture.completedFuture(null);
	        }

	        String normalizedSlackId = normalizeSlackId(userSlackId);

	        List<GptMessage> messages = buildContextMessages(slackUser, normalizedSlackId, query);

	        GptRequest gptRequest = buildGptRequest(messages, slackUser);

	        addFunctionsToRequest(gptRequest, gptFunctions);

	        saveGptRequest(gptRequest);

	        return getResponseFromGpt(gptRequest, slackUser);

	    } catch (Exception e) {
	        log.error("Error in getAnswerToSingleQuery", e);
	        return CompletableFuture.completedFuture(null);
	    }
	}

	private String normalizeSlackId(String slackId) {
	    return slackId.replaceAll("\\s+", "_");
	}

	private List<GptMessage> buildContextMessages(SlackUser slackUser, String normalizedSlackId, String query) {
	    List<GptMessage> requests = getLastRequestsOfUser(slackUser);
	    List<GptMessage> responses = getLastResponsesToUser(slackUser);

	    List<GptMessage> messages = new ArrayList<>();
	    int contextSize = Math.min(requests.size(), responses.size());

	    for (int i = contextSize - 1; i >= 0; i--) {
	        messages.add(requests.get(i));
	        messages.add(responses.get(i));
	    }

	    messages.add(getInitialSystemMessage(normalizedSlackId));
	    messages.add(new GptMessage(ROLE_USER, query, normalizedSlackId));

	    return messages.stream()
	            .filter(msg -> msg.getContent() != null && !msg.getContent().trim().isEmpty())
	            .toList();
	}

	private GptRequest buildGptRequest(List<GptMessage> messages, SlackUser slackUser) {
	    GptRequest gptRequest = new GptRequest();
	    gptRequest.setModel(model);
	    gptRequest.setTemperature(temperature);
	    gptRequest.setPresencePenalty(presencePenalty);
	    gptRequest.setMaxTokens(maxTokens);
	    gptRequest.setMessages(messages);
	    gptRequest.setAuthor(slackUser.getSlackId());
	    gptRequest.setAuthorRealname(slackUser.getRealName());
	    return gptRequest;
	}

	private void addFunctionsToRequest(GptRequest gptRequest, GptFunction... gptFunctions) {
	    if (gptFunctions != null && gptFunctions.length > 0) {
	        List<GptTool> tools = new ArrayList<>();
	        for (GptFunction function : gptFunctions) {
	            tools.add(new GptTool("function", function));
	        }
	        gptRequest.setTools(tools);
	    }
	}

	
	@Async("defaultExecutor")
	public CompletableFuture<String> getResponseFromGpt(GptRequest gptRequest, SlackUser slackUserRequestAuthor) throws IOException {
	    gptRequest.setAuthor(slackUserRequestAuthor.getSlackId());
	    gptRequest.setAuthorRealname(slackUserRequestAuthor.getRealName());
	    log.debug("slackUserRequestAuthor.getSlackId() : " + slackUserRequestAuthor.getSlackId());
	    log.debug("slackUserRequestAuthor.getRealName() : " + slackUserRequestAuthor.getRealName());
	    saveGptRequest(gptRequest);
	    log.debug("Sending request to GPT...");
	    return sendRequest(gptRequest, slackUserRequestAuthor, retryAttempts);
	}

	private CompletableFuture<String> sendRequest(GptRequest request, SlackUser slackUser, int retries) throws IOException {
	    for (int i = 0; i < retries; i++) {
	        try {
	            GptResponse response = restClient.post()
	                    .body(request)
	                    .retrieve()
	                    .body(GptResponse.class);

	            return extractGptResponseContent(request, response, slackUser);

	        } catch (RuntimeException e) {
	            log.debug("Attempt {} failed, retrying after {} seconds", i + 1, waitSeconds, e);
	            log.debug("Attempt {} failed: {}", i + 1, e.getMessage(), e);
	            try {
	                TimeUnit.SECONDS.sleep(waitSeconds);
	            } catch (InterruptedException ignored) {}
	        }
	    }
	    log.error("All {} retry attempts failed", retries);
	    return CompletableFuture.completedFuture(null);
	}

	
	@Async("defaultExecutor")
	private CompletableFuture<String> extractGptResponseContent(
	        GptRequest gptRequest,
	        GptResponse gptResponse,
	        SlackUser slackUserRequestAuthor) throws ParseException, IOException {

	    if (gptResponse == null || gptResponse.getChoices() == null || gptResponse.getChoices().isEmpty()) {
	        log.info("GPT response has no choices or is null");
	        return CompletableFuture.completedFuture("");
	    }

	    GptMessage message = gptResponse.getChoices().get(0).getMessage();
	    String content = message != null && message.getContent() != null ? message.getContent() : "";

	    gptResponse.setContent(content);
	    gptResponse.setRequestId(gptRequest.getId());
	    gptResponse.setRequestSlackID(gptRequest.getAuthor());
	    saveResponse(gptResponse);

	    List<GptMessage.Tool> toolCalls = message != null ? message.getToolCalls() : null;

	    if (toolCalls != null && !toolCalls.isEmpty()) {
	        return processToolCalls(gptRequest, toolCalls, slackUserRequestAuthor);
	    }

	    return CompletableFuture.completedFuture(content);
	}

	private CompletableFuture<String> processToolCalls(
	        GptRequest gptRequest,
	        List<GptMessage.Tool> toolCalls,
	        SlackUser slackUserRequestAuthor) {

	    for (GptMessage.Tool tool : toolCalls) {
	        GptMessage.Tool.FunctionCall functionCall = tool.getFunctionCall();
	        Function function = functionFactory.getFunctionByFunctionName(functionCall.getName());

	        log.info("Calling function '{}' with args: {}", functionCall.getName(), functionCall.getArguments());

	        CompletableFuture<String> functionResponse = function.execute(functionCall.getArguments());

	        return functionResponse.thenCompose(funcResp -> {
	            GptMessage gptMessage = new GptMessage();
	            gptMessage.setRole("function");
	            gptMessage.setContent(funcResp);
	            gptMessage.setName(functionCall.getName());

	            gptRequest.getMessages().add(gptMessage);
	            gptRequest.setFunctions(null); 

	            log.debug("Calling GPT with updated request after function call");
	            try {
	                return getResponseFromGpt(gptRequest, slackUserRequestAuthor);
	            } catch (IOException e) {
	                log.error("Error in getResponseFromGpt after function call", e);
	                return CompletableFuture.completedFuture(funcResp);
	            }
	        });
	    }

	    return CompletableFuture.completedFuture("");
	}

	
	
	public void saveGptRequest(GptRequest request) 
	{
		saveGptRequestToJsonFile(request);
		jpaGptRequestRepo.save(request);
	}
	
	public void saveResponse(GptResponse gptResponse)
	{
		saveGptResponseToJsonFile(gptResponse);
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
	
	public void saveGptRequestToJsonFile(GptRequest gptRequest) {
	    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
	    String filePath = "C:\\tmp\\JSON\\request\\request" + timestamp + ".json";
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), gptRequest);
	        System.out.println("Saved GptRequest to " + filePath);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void saveGptResponseToJsonFile(GptResponse gptResponse) {
	    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
	    String filePath = "C:\\tmp\\JSON\\response\\response" + timestamp + ".json";
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), gptResponse);
	        System.out.println("Saved GptResponse to " + filePath);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}


	
	
}