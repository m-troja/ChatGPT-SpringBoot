package com.michal.openai.gpt.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.*;
import com.michal.openai.functions.FunctionFacory;
import com.michal.openai.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@Import(GptServiceImplTest.LocalConfig.class)
class GptServiceImplTest {

    @MockitoBean JpaGptRequestRepo jpaGptRequestRepo;
    @MockitoBean JpaGptResponseRepo jpaGptResponseRepo;
    @MockitoBean JpaGptMessageRepo jpaGptMessageRepo;
    @MockitoBean JpaSlackRepo jpaSlackRepo;
    @MockitoBean FunctionFacory functionFactory;
    @MockitoBean(name = "gptRestClient") RestClient gptRestClient;
    @MockitoBean ObjectMapper objectMapper;

    GptServiceImpl service;

    String slackRequestJson;
    String gptRequestJson;
    String gptResponseJson;
    String model;
    Double temperature;
    Double presencePenalty;
    Integer maxTokens;
    Integer retryAttempts;
    Integer waitSeconds;
    Integer qtyOfContextMessages;
    String systemInitialMessage;
    String jsonDir;

    @BeforeEach
    void setup() throws Exception {
        slackRequestJson = Files.readString(Path.of("src/test/resources/SlackRequestSayHi.json"));
        gptRequestJson = Files.readString(Path.of("src/test/resources/GptRequestSayHi.json"));
        gptResponseJson = Files.readString(Path.of("src/test/resources/GptResponseSayHi.json"));

        service = new GptServiceImpl(
                jpaGptRequestRepo,
                gptRestClient,
                functionFactory,
                jpaGptResponseRepo,
                jpaGptMessageRepo,
                jpaSlackRepo,
                objectMapper,
                jpaGptMessageRepo
        );

        model = "gpt-4.1";
        temperature = 0.8;
        presencePenalty = 0.1;
        maxTokens = 4000;
        retryAttempts = 3;
        waitSeconds = 0;
        qtyOfContextMessages = 5;
        systemInitialMessage = "You are a SlackBot";
        jsonDir = "/tmp";
    }

    /*
     * Check GPT answered a string with no function call.
     * Empty message context.
     */
    @Test
    void checkAnswerNoFunctions() {
        SlackUser user = new SlackUser("U12345678", "Slack Name");
        when(jpaSlackRepo.findBySlackUserId("U12345678")).thenReturn(user);
        when(jpaGptRequestRepo.getLastRequestsBySlackId("U12345678", 5)).thenReturn(List.of());
        when(jpaGptResponseRepo.getLastResponsesToUser("U12345678", 5)).thenReturn(List.of());
        CompletableFuture<String> query = CompletableFuture.completedFuture("Just say hi");
        CompletableFuture<String> userName = CompletableFuture.completedFuture(user.getSlackName());
        GptResponse response = buildResponse("Hi there", false);

        String answerFromService = service.getAnswerWithSlack(query, userName).join();

        assertThat(answerFromService).isEqualTo("Hi there!");

    }

    // Utils
    GptResponse buildResponse(String content, boolean isRequestFunctionCall) {
        GptResponse response = new GptResponse();
        GptMessage message = new GptMessage("assistant", content);
        GptResponse.Choice choice = new GptResponse.Choice();
        choice.setMessage(message);
        response.setChoices(List.of(choice));
        return response;

    }

    @TestConfiguration
    static class LocalConfig {
        @Bean("gptRestClient")
        public RestClient gptRestClientMock() {
            return mock(RestClient.class);
        }
    }
}
