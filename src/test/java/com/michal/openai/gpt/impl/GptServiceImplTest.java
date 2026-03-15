package com.michal.openai.gpt.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.michal.openai.config.BeansConfiguration;
import com.michal.openai.exception.GptCommunicationException;
import com.michal.openai.gpt.entity.GptMessage;
import com.michal.openai.gpt.entity.GptResponse;
import com.michal.openai.gpt.service.impl.GptServiceImpl;
import com.michal.openai.gpt.tool.executor.ToolExecutor;
import com.michal.openai.gpt.tool.factory.ToolInvoker;
import com.michal.openai.gpt.tool.registry.GptToolRegistry;
import com.michal.openai.persistence.RequestDtoRepo;
import com.michal.openai.persistence.ResponseDtoRepo;
import com.michal.openai.persistence.SlackRepo;
import com.michal.openai.slack.entity.SlackUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(GptServiceImpl.class)
@Import(BeansConfiguration.class)
@TestPropertySource(properties = {
        "CHAT_MAX_TOKENS=100",
        "CHAT_JSON_DIR=tmp/log",
        "GPT_CHAT_SYSTEM_INITIAL_MESSAGE=TEST"
})
class GptServiceImplTest {

    private static final int qtyContextMessagesInRequestOrResponse = 2;
    private static final int totalQtyMessagesInContext = qtyContextMessagesInRequestOrResponse * 2 + 2; // Context + System/Initial Msg + query
    private static final String chatGptApiUrl = "https://api.openai.com/v1/chat/completions";

    @Autowired
    MockRestServiceServer server;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    GptServiceImpl service;

    @MockitoBean
    SlackRepo slackRepo;

    @MockitoBean
    RequestDtoRepo requestRepo;

    @MockitoBean
    ResponseDtoRepo responseRepo;

    @MockitoBean
    GptToolRegistry toolRegistry;

    @MockitoBean
    ToolInvoker toolInvoker;

    SlackUser slackUser;

    @BeforeEach
    void setup() {

        mapper.registerModule(new JavaTimeModule());

        slackUser = new SlackUser();
        slackUser.setSlackUserId("U123");

        when(slackRepo.findBySlackUserId("U123")).thenReturn(slackUser);

        when(requestRepo.findByUserSlackIdOrderByTimestampDesc(any(), any()))
                .thenReturn(List.of());

        when(responseRepo.findByUserSlackIdOrderByTimestampDesc(any(), any()))
                .thenReturn(List.of());

        when(toolRegistry.allAllowedGptTools()).thenReturn(List.of());

        service.setModel("gpt-5.1");
        service.setTemperature(0.7);
        service.setRetryAttempts(2);
        service.setWaitSeconds(0);
        service.setMaxTokens(100);
        service.setSystemInitialMessage("Hi ");
        service.setQtyContextMessagesInRequestOrResponse(2);
        service.setJsonDir("build/test-json");

        service.init();
    }
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Qualifier("gptRestClient")
        RestClient gptRestClient(RestClient.Builder builder) {
            return builder.baseUrl(chatGptApiUrl).build();
        }
    }


    /* ------------------------------------------------ */
    /* Helper methods                                   */
    /* ------------------------------------------------ */

    private String json(GptResponse response) throws Exception {
        return mapper.writeValueAsString(response);
    }

    private GptResponse assistantResponse(String text) {
        var message = new GptMessage("assistant", text);
        var choice = new GptResponse.Choice();
        choice.setMessage(message);

        var response = new GptResponse();
        response.setChoices(List.of(choice));
        return response;
    }

    private GptResponse toolCallResponse(String function) {

        var tool = new GptMessage.ToolCall("1", "function");
        var fc = new GptMessage.ToolCall.FunctionCall(function, "{\"key\":\"x\"}");
        tool.setFunctionCall(fc);

        var message = new GptMessage("assistant", null);
        message.setToolCalls(List.of(tool));

        var choice = new GptResponse.Choice();
        choice.setMessage(message);

        var resp = new GptResponse();
        resp.setChoices(List.of(choice));

        return resp;
    }

    /* ------------------------------------------------ */
    /* SUCCESS TESTS                                    */
    /* ------------------------------------------------ */

    @Test
    void shouldReturnAssistantAnswer() throws Exception {

        server.expect(times(1), anything())
                .andRespond(withSuccess(
                        json(assistantResponse("hello")),
                        MediaType.APPLICATION_JSON));

        String result = service.getAnswerWithSlack("hi", "U123");

        assertThat(result).isEqualTo("hello");
    }

    @Test
    void shouldCallToolsListFromRegistry() throws Exception {

        server.expect(anything())
                .andRespond(withSuccess(
                        json(assistantResponse("ok")),
                        MediaType.APPLICATION_JSON));

        service.getAnswerWithSlack("hello", "U123");

        verify(toolRegistry).allAllowedGptTools();
    }

    @Test
    void shouldHandleToolCallConversation() throws Exception {

        ToolExecutor executor = mock(ToolExecutor.class);

        when(toolRegistry.get("createTask")).thenReturn(executor);
        when(toolInvoker.invoke(any(), any())).thenReturn("done");

        server.expect(anything())
                .andRespond(withSuccess(
                        json(toolCallResponse("createTask")),
                        MediaType.APPLICATION_JSON));

        server.expect(anything())
                .andRespond(withSuccess(
                        json(assistantResponse("finished")),
                        MediaType.APPLICATION_JSON));

        String result = service.getAnswerWithSlack("create", "U123");

        assertThat(result).isEqualTo("finished");
    }

    @Test
    void shouldInvokeToolExecutor() throws Exception {

        ToolExecutor executor = mock(ToolExecutor.class);

        when(toolRegistry.get("toolX")).thenReturn(executor);
        when(toolInvoker.invoke(any(), any())).thenReturn("ok");

        server.expect(anything())
                .andRespond(withSuccess(
                        json(toolCallResponse("toolX")),
                        MediaType.APPLICATION_JSON));

        server.expect(anything())
                .andRespond(withSuccess(
                        json(assistantResponse("done")),
                        MediaType.APPLICATION_JSON));

        service.getAnswerWithSlack("run", "U123");

        verify(toolInvoker).invoke(eq(executor), any());
    }

    /* ------------------------------------------------ */
    /* FAILURE TESTS                                    */
    /* ------------------------------------------------ */

    @Test
    void shouldThrowOnEmptyChoices() throws Exception {

        GptResponse response = new GptResponse();
        response.setChoices(List.of());

        server.expect(anything())
                .andRespond(withSuccess(
                        json(response),
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() ->
                service.getAnswerWithSlack("hi", "U123"))
                .isInstanceOf(GptCommunicationException.class);
    }

    @Test
    void shouldRetryWhenHttpFails() {

        server.expect(times(2), anything())
                .andRespond(request -> {
                    throw new RuntimeException("HTTP error");
                });

        assertThatThrownBy(() ->
                service.getAnswerWithSlack("hi", "U123"))
                .isInstanceOf(GptCommunicationException.class);
    }

    @Test
    void shouldThrowOnInvalidJson() {

        server.expect(anything())
                .andRespond(withSuccess("invalid-json", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() ->
                service.getAnswerWithSlack("hi", "U123"))
                .isInstanceOf(GptCommunicationException.class);
    }

    @Test
    void shouldHandleToolExecutionError() throws Exception {

        ToolExecutor executor = mock(ToolExecutor.class);

        when(toolRegistry.get("toolA")).thenReturn(executor);
        when(toolInvoker.invoke(any(), any())).thenThrow(new RuntimeException());

        server.expect(anything())
                .andRespond(withSuccess(
                        json(toolCallResponse("toolX")),
                        MediaType.APPLICATION_JSON));

        server.expect(anything())
                .andRespond(withSuccess(
                        json(assistantResponse("done")),
                        MediaType.APPLICATION_JSON));

        String result = service.getAnswerWithSlack("test", "U123");

        assertThat(result).isEqualTo("done");
    }

    /* ------------------------------------------------ */
    /* CONTEXT TESTS                                    */
    /* ------------------------------------------------ */

    @Test
    void shouldBuildContextMessages() throws Exception {

        server.expect(anything())
                .andRespond(withSuccess(
                        json(assistantResponse("ok")),
                        MediaType.APPLICATION_JSON));

        service.getAnswerWithSlack("query", "U123");

        verify(requestRepo).findByUserSlackIdOrderByTimestampDesc(any(), any());
        verify(responseRepo).findByUserSlackIdOrderByTimestampDesc(any(), any());
    }

    @Test
    void shouldHandleMissingSlackUser() throws Exception {

        when(slackRepo.findBySlackUserId("U999")).thenReturn(null);

        server.expect(anything())
                .andRespond(withSuccess(
                        json(assistantResponse("hello")),
                        MediaType.APPLICATION_JSON));

        String result = service.getAnswerWithSlack("hello", "U999");

        assertThat(result).isEqualTo("hello");
    }

    /* ------------------------------------------------ */
    /* DATABASE TESTS                                   */
    /* ------------------------------------------------ */

    @Test
    void shouldClearDatabase() {

        service.clearDatabase();

        verify(requestRepo).deleteAll();
        verify(responseRepo).deleteAll();
    }

    @Test
    void shouldThrowWhenDatabaseClearFails() {

        doThrow(new RuntimeException()).when(responseRepo).deleteAll();

        assertThatThrownBy(() -> service.clearDatabase())
                .isInstanceOf(RuntimeException.class);
    }

    /* ------------------------------------------------ */
    /* ADDITIONAL EDGE TESTS                            */
    /* ------------------------------------------------ */

    @Test
    void shouldReturnAssistantContentEvenWithNullUser() throws Exception {

        when(slackRepo.findBySlackUserId(any())).thenReturn(null);

        server.expect(anything())
                .andRespond(withSuccess(
                        json(assistantResponse("response")),
                        MediaType.APPLICATION_JSON));

        String result = service.getAnswerWithSlack("test", "unknown");

        assertThat(result).isEqualTo("response");
    }

    @Test
    void shouldCallRetryOnlyConfiguredTimes() {

        server.expect(times(2), anything())
                .andRespond(req -> {
                    throw new RuntimeException();
                });

        assertThatThrownBy(() ->
                service.getAnswerWithSlack("retry", "U123"))
                .isInstanceOf(GptCommunicationException.class);
    }

}