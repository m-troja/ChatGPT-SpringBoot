package com.michal.openai.gpt.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.exception.GptCommunicationException;
import com.michal.openai.functions.Function;
import com.michal.openai.functions.FunctionFacory;
import com.michal.openai.functions.entity.GptFunction;
import com.michal.openai.functions.entity.GptTool;
import com.michal.openai.gpt.GptService;
import com.michal.openai.gpt.entity.GptMessage;
import com.michal.openai.gpt.entity.GptRequest;
import com.michal.openai.gpt.entity.GptResponse;
import com.michal.openai.gpt.entity.cnv.GptMessageCnv;
import com.michal.openai.gpt.entity.dto.RequestDto;
import com.michal.openai.gpt.entity.dto.ResponseDto;
import com.michal.openai.log.JsonSaver;
import com.michal.openai.persistence.RequestDtoRepo;
import com.michal.openai.persistence.ResponseDtoRepo;
import com.michal.openai.persistence.SlackRepo;
import com.michal.openai.slack.entity.SlackUser;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

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
//    @Value("${gpt.chat.presence.penalty}")
//    private Double presencePenalty;
    @Value("${CHAT_MAX_TOKENS}")
    private Integer maxTokens;
    @Value("${gpt.chat.sendrequest.retryattempts}")
    private Integer retryAttempts;
    @Value("${gpt.chat.sendrequest.waitforretry.seconds}")
    private Integer waitSeconds;
    @Value("${gpt.chat.qty.context.messages}")
    private Integer qtyContextMessagesInRequestOrResponse;
    private Integer totalQtyMessagesInContext; // Context + System/Initial Msg + query
    @Value("${GPT_CHAT_SYSTEM_INITIAL_MESSAGE}")
    private String systemInitialMessage;
    @Value("${CHAT_JSON_DIR}")
    private String jsonDir;
    @Qualifier("gptRestClient")
    private final RestClient restClient;
    private final FunctionFacory functionFactory;
    private final RequestDtoRepo requestDtoRepo;
    private final ResponseDtoRepo responseDtoRepo;
    private final SlackRepo slackRepo;
    private final ObjectMapper objectMapper;

    // Constructor needed because of @Qualifier("gptRestClient")

    public GptServiceImpl(@Qualifier("gptRestClient") RestClient restClient, FunctionFacory functionFactory, RequestDtoRepo requestDtoRepo, ResponseDtoRepo responseDtoRepo, SlackRepo slackRepo, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.functionFactory = functionFactory;
        this.requestDtoRepo = requestDtoRepo;
        this.responseDtoRepo = responseDtoRepo;
        this.slackRepo = slackRepo;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        // Context of 2 last messages + 2 last responses + system message + last query
        totalQtyMessagesInContext = qtyContextMessagesInRequestOrResponse * 2 + 2;
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
                            throw new CompletionException(e);
                });
	}

    @Override
    public void clearDatabase() {
        try {
            log.debug("Trying to clear database...");
            responseDtoRepo.deleteAll();
            requestDtoRepo.deleteAll();
            log.debug("Database cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing database: ", e);
            throw new RuntimeException(e);
        }
    }

    private String handleQuery(String query, String slackUserId, GptFunction... gptFunctions) {
        SlackUser slackUserRequestAuthor = getSlackUserBySlackId(slackUserId);
        log.debug("Handle query: slackUserId={}, slackUserRequestAuthor={}", slackUserId, slackUserRequestAuthor);
        List<GptFunction> functions = List.of(gptFunctions);
        GptRequest gptRequest = buildGptRequest(query, slackUserRequestAuthor, functions);

        return callGptNoFunction(gptRequest, slackUserRequestAuthor);
    }

    private GptRequest buildGptRequest(String query, SlackUser slackUserRequestAuthor, List<GptFunction> gptFunctions ) {
        log.debug("Building GPT request with params: \nQuery:{}, \nSlackUser:{}, \nFunctions:{}", query, slackUserRequestAuthor, gptFunctions );
        GptRequest gptRequest = new GptRequest();
        List<GptMessage> messages = buildLastMessagesContextOfUserSlackId(slackUserRequestAuthor.getSlackUserId(), query);
        gptRequest.setMessages(messages);

        gptRequest.setModel(model);
        gptRequest.setTemperature(temperature);
//        gptRequest.setPresencePenalty(presencePenalty);
        gptRequest.setMaxOutputTokens(maxTokens);
        gptRequest.setMessages(messages);
        log.debug("Found {} GPT Functions to add into requestTools", gptFunctions.size());
        List<GptTool> requestTools = new ArrayList<>();
        gptFunctions.forEach(fn -> {
            requestTools.add(new GptTool("function", fn));
            log.debug("Added function into requestTools: {}", fn.getName());
        });
        gptRequest.setTools(requestTools);
        saveGptRequest(gptRequest);

        return gptRequest;
    }

    private String callGptNoFunction(GptRequest gptRequest, SlackUser slackUserRequestAuthor) {
        GptResponse gptResponse = sendRequestToGpt(gptRequest);
        if (gptResponse != null && gptRequest != null) {
            saveDto(gptRequest, gptResponse, slackUserRequestAuthor);
        } else {
            log.debug("GptResponse or GptRequest is null!");
        }

        if (gptResponse.getChoices() == null || gptResponse.getChoices().isEmpty()) {
            log.error("GPT response is null or empty");
            return "GPT response error";
        }

        GptMessage responseMessage = gptResponse.getChoices().getFirst().getMessage();
        List<GptMessage.ToolCall> toolsToCall = responseMessage.getToolCalls();

        if (toolsToCall == null || toolsToCall.isEmpty()) {
            return responseMessage.getContent();
        }

        for (GptMessage.ToolCall toolCall : toolsToCall) {
            log.debug("Tool call found, GPT will be called: {}", toolCall.getFunctionCall().getName());
            Function fn = functionFactory.getFunctionByFunctionName(toolCall.getFunctionCall().getName());
            String result;
            try {
                result = fn.execute(toolCall.getFunctionCall().getArguments());
                log.debug("Result of function: {}", result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            GptMessage toolMessage = new GptMessage();
            toolMessage.setRole("tool");
            toolMessage.setContent(result);
//            toolMessage.setName(toolCall.getFunctionCall().getName());
            toolMessage.setToolCallId(toolCall.getId());
            if (gptRequest != null) {
                gptRequest.getMessages().add(responseMessage);
                gptRequest.getMessages().add(toolMessage);
            }
            log.debug("List of messages to send to GPT:");
            log.debug("{}", gptRequest.getMessages() );

            while (gptRequest.getMessages().size() > totalQtyMessagesInContext) {
                log.debug("Context exceeded! Removed first message: {}", gptRequest.getMessages().getFirst());
                gptRequest.getMessages().removeFirst();
            }
        }
        return callGptNoFunction(gptRequest, slackUserRequestAuthor);
    }

    private GptResponse sendRequestToGpt(GptRequest gptRequest) {
        for (int attempt = 1; 1 <= retryAttempts; attempt++) {
//        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                log.debug("Calling GPT: attempt {}/{}", attempt, retryAttempts);
                log.debug("Sending to GPT:");
                logPrettyJson(gptRequest);
                var gptResponse = restClient.post()
                        .body(gptRequest)
                        .retrieve()
                        .body(GptResponse.class);
                log.debug("RestClient response:");
                log.debug("{}", gptResponse);

                if (gptResponse == null || gptResponse.getChoices() == null ||  gptResponse.getChoices().isEmpty()) {
                    log.error("GPT returned null response!");
                    throw new GptCommunicationException("GPT returned null response");
                }
                return gptResponse;

            } catch (RuntimeException e) {

                log.error("GPT request failed on attempt {} of {}.", attempt, retryAttempts, e);

                if (attempt == retryAttempts) {
                    log.error("Max retry attempts reached.");
                    throw new GptCommunicationException("Max retry attempts reached");
                }
                sleep(waitSeconds);
            }
        }
        return null;
    }

    private void saveDto(GptRequest request, GptResponse response, SlackUser slackUserRequestAuthor) {
        log.debug("Saving DTOs... ");
        var messageCnv = new GptMessageCnv();
        var requestMessage = request.getMessages().getLast();
        var responseMessage = response.getChoices().getFirst().getMessage();

        RequestDto requestDto;
        ResponseDto responseDto;

        if (requestMessage.getToolCallId() == null && requestMessage.getToolCalls() == null && requestMessage.getRole() != "tool") {
            requestDto = messageCnv.requestEntityToDto(requestMessage, slackUserRequestAuthor);
            requestDtoRepo.save(requestDto);
            saveMessageJson(requestDto);
        }
        else {
            log.debug("Tool call found in requestMessage, skipping RequestDTO creation");
        }

        if (responseMessage.getToolCalls() == null) {
            responseDto = messageCnv.responseEntityToDto(responseMessage, slackUserRequestAuthor);
            responseDtoRepo.save(responseDto);
            saveMessageJson(responseDto);
        } else {
            log.debug("Tool call found in responseMessage, skipping ResponseDTO creation");
        }

        saveGptResponse(response);
        saveGptRequest(request);
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
	}

    private void saveGptResponse(GptResponse gptResponse) {
//        log.debug("Response from GPT:");
//        logPrettyJson(gptResponse);
        JsonSaver jsonSaver = new JsonSaver(jsonDir);
        jsonSaver.saveResponse(gptResponse);
	}

    private void saveMessageJson(Object obj) {
//        log.debug("Saved message:");
//        logPrettyJson(obj);
        JsonSaver jsonSaver = new JsonSaver(jsonDir);
        jsonSaver.saveMessage(obj);
    }

	/* Get last messages of user to build context for GPT */

    private List<GptMessage> getLastRequestsOfUser(SlackUser user) {
		log.debug("Calling getLastRequestsOfUser with user={}, max allowed context= {}", user.getSlackUserId(), qtyContextMessagesInRequestOrResponse);
        PageRequest limit = PageRequest.of(0, qtyContextMessagesInRequestOrResponse);
        log.debug("Limit of requests: {}", limit);

        List<RequestDto> dtos = requestDtoRepo.findByUserSlackIdOrderByTimestampDesc(user.getSlackUserId(), limit);
		if (dtos.isEmpty()) {
            log.debug("Not found any request");
        }

        List<GptMessage> messages = new ArrayList<>();
		for (RequestDto dto : dtos)
		{
            var gptMessage = new GptMessage(dto.getRole(), dto.getContent());
			log.debug("Found request: {}, authorSlackID: {}", dto.getContent(), user.getSlackUserId() );
            messages.add(gptMessage);
		}
		return messages;
	}

	/* Get last responses to user to build context for GPT */

    private List<GptMessage> getLastResponsesToUser(SlackUser user) {
        log.debug("Calling getLastResponsesToUser with user={}, max allowed context= {}", user.getSlackUserId(), qtyContextMessagesInRequestOrResponse);
        PageRequest limit = PageRequest.of(0, qtyContextMessagesInRequestOrResponse);
        log.debug("Limit of responses: {}", limit);

        List<ResponseDto> dtos = responseDtoRepo.findByUserSlackIdOrderByTimestampDesc(user.getSlackUserId(), limit);
        if (dtos.isEmpty()) {
            log.debug("Not found any responses");
        }

        List<GptMessage> messages = new ArrayList<>();
        for (ResponseDto dto : dtos)
        {
            var gptMessage = new GptMessage(dto.getRole(), dto.getContent());
            log.debug("Found response: {}, authorSlackID: {}", dto.getContent(), user.getSlackUserId() );
            messages.add(gptMessage);
        }
        return messages;
	}

    private List<GptMessage> buildLastMessagesContextOfUserSlackId(String userSlackId, String query) {
        List<GptMessage> requests = getLastRequestsOfUser(getSlackUserBySlackId(userSlackId));
        List<GptMessage> responses = getLastResponsesToUser(getSlackUserBySlackId(userSlackId));
        List<GptMessage> messages = new ArrayList<>();

        int contextSize = Math.min(requests.size(), responses.size());
        for (int i = contextSize - 1; i >= 0; i--) {
            GptMessage response = responses.get(i);
            GptMessage request = requests.get(i);

            if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
                GptMessage toolMessage = new GptMessage();
                toolMessage.setRole("tool");
                toolMessage.setContent(response.getContent());
                toolMessage.setName(response.getName());
                toolMessage.setToolCalls(response.getToolCalls());
                messages.add(toolMessage);
            } else {
                messages.add(response);
            }
            messages.add(request);
        }

        messages.add(getInitialSystemMessage(userSlackId));
        messages.add(new GptMessage(ROLE_USER, query));

        while (messages.size() > totalQtyMessagesInContext) {
            log.debug("Context exceeded, removing first message: {}", messages.getFirst());
            messages.removeFirst();
        }

        log.debug("Added total {} messages into context", messages.size());
        return messages;
    }

	private GptMessage getInitialSystemMessage(String userSlackId) {
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
        return slackRepo.findBySlackUserId(slackId);
    }
}