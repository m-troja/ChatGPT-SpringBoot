package com.michal.openai.gpt.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.*;
import com.michal.openai.functions.FunctionFacory;
import com.michal.openai.persistence.*;
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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(GptServiceImpl.class)
@Import(GptServiceImplTest.TestConfig.class)
@TestPropertySource(properties = {
        "CHAT_MAX_TOKENS=100"})
class GptServiceImplTest {

    private static String chatGptApiUrl = "https://api.openai.com/v1/chat/completions";
    @Autowired MockRestServiceServer server;
    @Autowired ObjectMapper objectMapper;
    @Autowired GptServiceImpl service;

    @MockitoBean JpaGptRequestRepo jpaGptRequestRepo;
    @MockitoBean JpaGptResponseRepo jpaGptResponseRepo;
    @MockitoBean JpaGptMessageRepo jpaGptMessageRepo;
    @MockitoBean JpaSlackRepo jpaSlackRepo;
    @MockitoBean FunctionFacory functionFactory;

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
    void checkAnswerNoFunctions() throws JsonProcessingException {
        SlackUser user = new SlackUser("U12345678", "Slack Name");
        when(jpaSlackRepo.findBySlackUserId("U12345678")).thenReturn(user);
        when(jpaGptRequestRepo.getLastRequestsBySlackId("U12345678", 5)).thenReturn(List.of());
        when(jpaGptResponseRepo.getLastResponsesToUser("U12345678", 5)).thenReturn(List.of());

        GptResponse gptResponse = buildResponse("Hi there!", false);
        System.out.println(gptResponse.toString()); // to be deleted
        mockGptResponse(gptResponse);

        CompletableFuture<String> query = CompletableFuture.completedFuture("Just say hi");
        CompletableFuture<String> userName = CompletableFuture.completedFuture(user.getSlackUserId());

        String answerFromService = service.getAnswerWithSlack(query, userName).join();

        assertThat(answerFromService).isEqualTo("Hi there!");
    }

    private GptResponse buildResponse(String content, boolean isRequestFunctionCall) {
        GptResponse response = new GptResponse();
        GptMessage message = new GptMessage("assistant", content);
        GptResponse.Choice choice = new GptResponse.Choice();
        choice.setMessage(message);
        response.setChoices(List.of(choice));
        return response;
    }

    private void mockGptResponse(GptResponse gptResponse) throws JsonProcessingException {
        server.expect(requestTo(chatGptApiUrl))
                .andRespond(withSuccess(objectMapper.writeValueAsString(gptResponse), MediaType.APPLICATION_JSON));

    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Qualifier("gptRestClient")
        RestClient gptRestClient(RestClient.Builder builder) {
            return builder.baseUrl("https://api.openai.com/v1/chat/completions").build();
        }
    }
}
