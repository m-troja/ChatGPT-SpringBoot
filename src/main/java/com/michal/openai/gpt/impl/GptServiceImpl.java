package com.michal.openai.gpt.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.functions.Function;
import com.michal.openai.log.JsonSaver;
import com.michal.openai.persistence.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.michal.openai.entity.GptFunction;
import com.michal.openai.entity.GptMessage;
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
public class GptServiceImpl implements GptService {

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
    private JpaGptRequestRepo jpaGptRequestRepo;
    @Qualifier("gptRestClient")
    private final RestClient restClient;
    private final FunctionFacory functionFactory;
    private final JpaGptResponseRepo jpaGptResponseRepo;
    private final JpaGptMessageRepo messageRepo;
    private final JpaSlackRepo jpaSlackrepo;
    private final JpaGptMessageRepo jpaMessageRepo;
    private final ObjectMapper objectMapper;

    // Constructor needed because of @Qualifier("gptRestClient")
    public GptServiceImpl(JpaGptRequestRepo jpaGptRequestRepo,  @Qualifier("gptRestClient") RestClient restClient, FunctionFacory functionFactory, JpaGptResponseRepo jpaGptResponseRepo,
                          JpaGptMessageRepo messageRepo, JpaSlackRepo jpaSlackrepo, ObjectMapper objectMapper, JpaGptMessageRepo jpaMessageRepo) {
        this.jpaGptRequestRepo = jpaGptRequestRepo;
        this.restClient = restClient;
        this.functionFactory = functionFactory;
        this.jpaGptResponseRepo = jpaGptResponseRepo;
        this.messageRepo = messageRepo;
        this.jpaSlackrepo = jpaSlackrepo;
        this.objectMapper = objectMapper;
        this.jpaMessageRepo = jpaMessageRepo;
    }
	/*
	 * Called by SlackAPI controller.
	 * Builds object of GPTRequest and forwards it to send to GPT.
	 */
	@Override
	public CompletableFuture<String> getAnswerWithSlack(CompletableFuture<String> queryFuture, CompletableFuture<String> userNameFuture, GptFunction... gptFunctions ) {

        return queryFuture.thenCombine(userNameFuture, (query, slackUserId) -> handleQuery(query, slackUserId, gptFunctions))
                .exceptionally(e -> {
                            log.error("Async error:", e);
                            return "Error calling GPT";
                });
	}

    private String handleQuery(String query, String slackUserId, GptFunction... gptFunctions) {
        SlackUser slackUserRequestAuthor = getSlackUserBySlackId(slackUserId);
        log.debug("Handle query: slackUserId={}, slackUserRequestAuthor={}", slackUserId, slackUserRequestAuthor);
        List<GptFunction> functions = List.of(gptFunctions);
        GptRequest gptRequest = buildGptRequest(query, slackUserRequestAuthor, functions);

        return callGptNoFunction(gptRequest);
    }

    private GptRequest buildGptRequest(String query, SlackUser slackUserRequestAuthor, List<GptFunction> gptFunctions )
    {
        log.debug("Building GPT request with params: \nQuery:{}, \nSlackUser:{}, \nFunctions:{}", query, slackUserRequestAuthor, gptFunctions );
        GptRequest gptRequest = new GptRequest();
        List<GptMessage> messages = getLastMessagesOfUserSlackId(slackUserRequestAuthor.getSlackUserId(), gptRequest, query);

        /* Define request parameters */
        gptRequest.setAuthor(slackUserRequestAuthor.getSlackUserId());
        gptRequest.setContent(query);
        gptRequest.setAuthorRealname(slackUserRequestAuthor.getSlackName());
        gptRequest.setModel(model);
        gptRequest.setTemperature(temperature);
        gptRequest.setPresencePenalty(presencePenalty);
        gptRequest.setMaxOutputTokens(maxTokens);
        gptRequest.setMessages(messages);
        List<GptTool> requestTools = new ArrayList<>();
        log.debug("Found {} GPT Functions to add into requestTools", gptFunctions.size());
        gptFunctions.forEach(fn -> {
            requestTools.add(new GptTool("function", fn));
            log.debug("Added function into requestTools: {}", fn.getName());
        });
        gptRequest.setTools(requestTools);
        gptRequest.setAuthor(slackUserRequestAuthor.getSlackUserId());
        gptRequest.setAuthorRealname(slackUserRequestAuthor.getSlackName());
        return gptRequest;
    }

	private String callGptNoFunction(GptRequest gptRequest)
	{
        GptResponse gptResponse = sendRequestToGpt(gptRequest);
        GptMessage lastMessage = gptResponse.getChoices().getFirst().getMessage();

        List<GptMessage.Tool> toolsToCall = lastMessage.getToolCalls();
        if (toolsToCall == null || toolsToCall.isEmpty())
        {
            String content = lastMessage.getContent();
            log.debug("No tool calls found, returning message from GPT: {}", content);
            return content;
        }
        for (GptMessage.Tool tool : toolsToCall) {
            log.debug("Found tool call, GPT will be called: {}", tool);
            Function fn = functionFactory.getFunctionByFunctionName(tool.getFunctionCall().getName());
            String functionResult = fn.execute(tool.getFunctionCall().getArguments());
            log.debug("Result of function call: {}", functionResult);
            GptMessage toolMessage = new GptMessage();
            toolMessage.setRole("function");
            toolMessage.setContent(functionResult);
            toolMessage.setName(tool.getFunctionCall().getName());
            gptRequest.getMessages().add(toolMessage);
        }
        return callGptNoFunction(gptRequest);
	}

    private GptResponse sendRequestToGpt(GptRequest gptRequest) {
        saveGptRequest(gptRequest);

        for (int attempt = 1; attempt <= retryAttempts; attempt++) {

            try {
                log.debug("Calling GPT attempt {}/{}", attempt, retryAttempts);

                var gptResponse = restClient.post()
                        .body(gptRequest)
                        .retrieve()
                        .body(GptResponse.class);

                if (gptResponse == null) {
                    log.error("GPT returned null response!");
                    return null;
                }
                saveRequestAndResponseIntoDb(gptRequest, gptResponse);
                return gptResponse;

            } catch (RuntimeException e) {

                log.error("GPT request failed on attempt {} of {}.", attempt, retryAttempts, e);

                if (attempt == retryAttempts) {
                    log.error("Max retry attempts reached.");
                    break;
                }

                sleep(waitSeconds);
            }
        }
        return null;
    }

    private void saveRequestAndResponseIntoDb(GptRequest req, GptResponse resp) {
        // Persistence - save data into DB

        if (req.getId() != null) {
            resp.setRequestId(req.getId());
        }
        else {
            resp.setRequestId(0L);
            log.error("GPT Request was null");
        }

        boolean isFunctionCall ;
        String content;
        GptMessage.Tool.FunctionCall functionCall ;
        if (resp.getChoices().getFirst().getMessage().getToolCalls() != null)
        {
            isFunctionCall = true;
            functionCall = resp.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall();
            content = String.format("fn: %s, args: %s", functionCall.getName(), functionCall.getArguments());

        }
        else {
            isFunctionCall = false;
            content = resp.getChoices().getFirst().getMessage().getContent();
        }
        resp.setFunctionCall(isFunctionCall);
        resp.setContent(content);
        Long requestId = resp.getRequestId();
        resp.setRequestId(requestId);
        String requestSlackId = req.getAuthor();
        resp.setRequestSlackID(requestSlackId);
        String authorRealName = req.getAuthorRealname();
        resp.setRequestAuthorRealName(authorRealName);
        saveResponse(resp);
        log.debug("GptResponseId {}: RequestSlackID={}, Content={}, RequestId={}, isFunctionCall={}", requestId, requestSlackId, content, requestId, isFunctionCall);
    }

    private void sleep(int seconds) {
        try {
            log.debug("Sleeping for {}s", seconds);
            TimeUnit.SECONDS.sleep(seconds);

        } catch (InterruptedException e) {
            log.error("Sleeping interrupted ", e);
            throw new RuntimeException(e);
        }
    }

	private void saveGptRequest(GptRequest gptRequest)  {
        log.debug("Request to GPT:");
        logPrettyJson(gptRequest);
        JsonSaver jsonSaver = new JsonSaver(jsonDir);
        jsonSaver.saveRequest(gptRequest);
		jpaGptRequestRepo.save(gptRequest);
	}

    private void saveResponse(GptResponse gptResponse) {
        log.debug("Response from GPT:");
        logPrettyJson(gptResponse);
        JsonSaver jsonSaver = new JsonSaver(jsonDir);
        jsonSaver.saveResponse(gptResponse);
		jpaGptResponseRepo.save(gptResponse);
	}

    private void saveGptMessage(GptMessage gptMessage) {
        log.debug("Saving GPT Message: {} ", gptMessage);
        jpaMessageRepo.save(gptMessage);
    }

	/* Get last messages of user to build context for GPT */

    private List<GptMessage> getLastRequestsOfUser(SlackUser user)
	{
		log.debug("Calling getLastRequestsOfUser with user={}, max allowed context= {}", user.getSlackUserId(), qtyOfContextMessages);
		
		List<String> messages = jpaGptRequestRepo.getLastRequestsBySlackId(user.getSlackUserId(), qtyOfContextMessages);
		if (messages.isEmpty()) {
            log.debug("Not found any request");
        }

        List<GptMessage> gptMessages = new ArrayList<>();
		for (String message : messages)
		{
			GptMessage gptMessage = new GptMessage( ROLE_USER, message, user.getSlackUserId()  );
			log.debug("Found request: {}, authorSlackID: {}", message, user.getSlackUserId() );
			gptMessages.add(gptMessage);
		}
		return gptMessages;
	}
	
	/* Get last responses to user to build context for GPT */

    private List<GptMessage> getLastResponsesToUser(SlackUser user)
	{
        log.debug("Calling getLastResponsesToUser with user={}, max allowed context={}", user.getSlackUserId(), qtyOfContextMessages);
		
		List<String> messages = jpaGptResponseRepo.getLastResponsesToUser(user.getSlackUserId(), qtyOfContextMessages);
        if (messages.isEmpty()) {
            log.debug("Not found any responses");
        }

        List<GptMessage> gptMessages = new ArrayList<>();
		for (String message : messages)
		{
			GptMessage gptMessage = new GptMessage( ROLE_ASSISTANT, message, user.getSlackUserId()  );
            log.debug("Found response: {}", message);
			gptMessages.add(gptMessage);
		}
		return gptMessages;
	}

    private List<GptMessage> getLastMessagesOfUserSlackId(String userSlackId, GptRequest gptRequest, String query)
    {
        /* Define list of messages to sent to GPT */

        List<GptMessage> requests = getLastRequestsOfUser(getSlackUserBySlackId(userSlackId));
        List<GptMessage> responses = getLastResponsesToUser(getSlackUserBySlackId(userSlackId));
        List<GptMessage> messages = new ArrayList<>();

        GptMessage message = new GptMessage(ROLE_USER, query, userSlackId);

        int contextSize = Math.min(requests.size(), responses.size());

        for (int i = contextSize - 1 ;  i >= 0; i--) {
            messages.add(requests.get(i));
            messages.add(responses.get(i));
        }
        messages.add(getInitialSystemMessage(userSlackId));
        messages.add(message);

        log.debug("Added total {} messages into context", messages.size() );
        return messages;
    }

	private GptMessage getInitialSystemMessage(String userSlackId)
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
            log.error("Error creating pretty JSON: ", e);
        }
    }

    public SlackUser getSlackUserBySlackId(String slackId) {
        return jpaSlackrepo.findBySlackUserId(slackId);
    }
	
}