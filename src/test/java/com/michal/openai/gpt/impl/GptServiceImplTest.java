package com.michal.openai.gpt.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.*;
import com.michal.openai.functions.FunctionFacory;
import com.michal.openai.functions.impl.AssignTaskSystemIssueFunction;
import com.michal.openai.persistence.*;
import com.michal.openai.tasksystem.entity.TaskSystemAssignIssueParameterProperties;
import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(GptServiceImpl.class)
@Import(GptServiceImplTest.TestConfig.class)
@TestPropertySource(properties = {
        "CHAT_MAX_TOKENS=100",
        "gpt.function.tasksystem.assign.issue.desc",
        "gpt.function.tasksystem.assign.issue.name",
        "gpt.function.tasksystem.assign.issue.attr.key.desc",
        "gpt.function.tasksystem.assign.issue.attr.slackUserId.desc"
        })
class GptServiceImplTest {

    private static final String chatGptApiUrl = "https://api.openai.com/v1/chat/completions";
    @Autowired MockRestServiceServer server;
    @Autowired ObjectMapper objectMapper;
    @Autowired GptServiceImpl service;

    @MockitoBean JpaGptRequestRepo jpaGptRequestRepo;
    @MockitoBean JpaGptResponseRepo jpaGptResponseRepo;
    @MockitoBean JpaGptMessageRepo jpaGptMessageRepo;
    @MockitoBean JpaSlackRepo jpaSlackRepo;
    @MockitoBean FunctionFacory functionFactory;
    @MockitoBean AssignTaskSystemIssueFunction function;
    @MockitoBean List<GptFunction> functions;

    @BeforeEach
    void setup() {
        service.setQtyOfContextMessages(5);
        service.setModel("gpt-4.1");
        service.setTemperature(0.8);
        service.setRetryAttempts(3);
        service.setPresencePenalty(1.0);
        service.setWaitSeconds(0);
        service.setMaxTokens(100);
        service.setSystemInitialMessage("Hi");
    }

    @Test
    void shouldAnswerNoFunctionCall() throws JsonProcessingException {
        SlackUser user = new SlackUser("U12345678", "Slack Name");
        when(jpaSlackRepo.findBySlackUserId("U12345678")).thenReturn(user);
        when(jpaGptRequestRepo.getLastRequestsBySlackId("U12345678", 5)).thenReturn(List.of());
        when(jpaGptResponseRepo.getLastResponsesToUser("U12345678", 5)).thenReturn(List.of());
        when(jpaGptRequestRepo.save(any())).thenAnswer(inv -> {
            GptRequest req = inv.getArgument(0);
            req.setId(1L);
            return req;
        });
        var gptResponse = buildResponseNoFunctionCall("Hi there!", false);

        // mock GptResponse
        server.expect(requestTo(chatGptApiUrl)).andRespond(withSuccess(objectMapper.writeValueAsString(gptResponse), MediaType.APPLICATION_JSON));

        CompletableFuture<String> query = CompletableFuture.completedFuture("Just say hi");
        CompletableFuture<String> userName = CompletableFuture.completedFuture(user.getSlackUserId());

        String answerFromService = service.getAnswerWithSlack(query, userName).join();

        assertThat(answerFromService).isEqualTo("Hi there!");
    }
    @Test
    void taskSystemAssignIssueFunctionTest() throws JsonProcessingException {
        // given
        var expectedDtoString = objectMapper.writeValueAsString(new TaskSystemIssueDto("Dummy-1", "Title", "Desc", "NEW", "HIGH", "U12345678" ,"U12345678"));
        SlackUser slackRequestAuthor = new SlackUser("U12345678", "Slack Name");
        CompletableFuture<String> query = CompletableFuture.completedFuture("Assign ticket Dummy-1 to U12345678");
        CompletableFuture<String> userName = CompletableFuture.completedFuture("U12345678");
        var gptResponseFunctionCall = buildResponseNoFunctionCall(null, true);
        var gptRequest = buildGptRequestFunctionCall(query.join(), slackRequestAuthor, functions);
        // when
        when(jpaSlackRepo.findBySlackUserId("U12345678")).thenReturn(slackRequestAuthor);
        when(jpaGptRequestRepo.getLastRequestsBySlackId("U12345678", 5)).thenReturn(List.of());
        when(jpaGptResponseRepo.getLastResponsesToUser("U12345678", 5)).thenReturn(List.of());
        when(jpaGptRequestRepo.save(any())).thenAnswer(inv -> {
            GptRequest req = inv.getArgument(0);
            req.setId(1L);
            return req;
        });
        when(functionFactory.getFunctionByFunctionName("assignTaskSystemIssueFunction")).thenReturn(function);
        when(function.execute(anyString())).thenReturn("OK");
        // then

        // mock GptResponse - function call
        server.expect(requestTo(chatGptApiUrl)).andRespond(withSuccess(objectMapper.writeValueAsString(gptResponseFunctionCall), MediaType.APPLICATION_JSON));

        var gptResponseNoFunctionCall = buildResponseNoFunctionCall(expectedDtoString, false);

        // mock GptResponse - no function call
        server.expect(requestTo(chatGptApiUrl)).andRespond(withSuccess(objectMapper.writeValueAsString(gptResponseNoFunctionCall), MediaType.APPLICATION_JSON));
        assertThat(service.getAnswerWithSlack(query, userName).join()).isEqualTo(expectedDtoString);

        assertThat(gptResponseFunctionCall.getChoices().getFirst().getMessage().getToolCalls()).isNotNull();
        assertThat(gptResponseFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().functionCall().name()).isEqualTo("assignTaskSystemIssueFunction");
        assertThat(gptResponseFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().functionCall().arguments()).isEqualTo("{\\\"key\\\":\\\"Dummy-1\\\",\\\"slackUserId\\\":\\\"U12345678\\\"}");

        assertThat(gptResponseNoFunctionCall.getChoices().getFirst().getMessage().getToolCalls()).isNull();
        assertThat(gptResponseNoFunctionCall.getChoices().getFirst().getMessage().getContent()).isEqualTo(expectedDtoString);
    }



    // Utils

    private GptResponse buildResponseNoFunctionCall(String content, boolean isRequestFunctionCall) {
        var response = new GptResponse();
        var choice = new GptResponse.Choice();
        var message = new GptMessage("assistant", content);
        if (!isRequestFunctionCall) {
            choice.setMessage(message);
            response.setChoices(List.of(choice));
            return response;
        }
        else {
            assertThat(content).isNull();
            var functionCall = new GptMessage.Tool.FunctionCall("assignTaskSystemIssueFunction", "{\\\"key\\\":\\\"Dummy-1\\\",\\\"slackUserId\\\":\\\"U12345678\\\"}");
            var tool = new GptMessage.Tool("id", "function", functionCall);
            message.setToolCalls(List.of(tool));
            choice.setMessage(message);
            response.setChoices(List.of(choice));
            return response;
        }
    }

    private GptRequest buildGptRequestFunctionCall(String query, SlackUser slackUserRequestAuthor, List<GptFunction> functions) {
        var gptRequest = new GptRequest();
        gptRequest.setId(1L);
        gptRequest.setAuthor(slackUserRequestAuthor.getSlackUserId());
        gptRequest.setContent(query);
        gptRequest.setAuthorRealname(slackUserRequestAuthor.getSlackName());
        List<GptTool> requestTools = new ArrayList<>();
        functions.forEach( f -> {
            requestTools.add(new GptTool("function", f));
        });
        gptRequest.setTools(requestTools);
        return gptRequest;
    }

    private void mockGptResponse(GptResponse gptResponse) throws JsonProcessingException {
        server.expect(requestTo(chatGptApiUrl)).andRespond(withSuccess(objectMapper.writeValueAsString(gptResponse), MediaType.APPLICATION_JSON));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Qualifier("gptRestClient")
        RestClient gptRestClient(RestClient.Builder builder) {
            return builder.baseUrl(chatGptApiUrl).build();
        }

        @Bean("defineAssignTaskSystemIssueFunction")
        public GptFunction assignTaskSystemIssue(
                @Value("${gpt.function.tasksystem.assign.issue.desc}") String functionDescription,
                @Value("${gpt.function.tasksystem.assign.issue.name}") String functionName,
                @Value("${gpt.function.tasksystem.assign.issue.attr.key.desc}") String attrKey,
                @Value("${gpt.function.tasksystem.assign.issue.attr.slackUserId.desc}") String attrSlackUserId

        ) {
            var getReposFunction = new GptFunction();
            getReposFunction.setName(functionName);
            getReposFunction.setDescription(functionDescription);

            GptFunction.Parameters gptFunctionParameters = getReposFunction.new Parameters();
            gptFunctionParameters.setType("object");
            gptFunctionParameters.setRequired(new String[] {attrKey, attrSlackUserId});

            var properties = new TaskSystemAssignIssueParameterProperties();
            var key = properties.new Key("string", attrKey);
            var slackUserId = properties.new SlackUserId("string", attrSlackUserId);

            properties.setKey(key);
            properties.setSlackUserId(slackUserId);
            gptFunctionParameters.setProperties(properties);
            getReposFunction.setParameters(gptFunctionParameters);

            return getReposFunction;
        }
    }
}
