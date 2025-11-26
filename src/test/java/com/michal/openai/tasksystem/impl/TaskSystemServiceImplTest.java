package com.michal.openai.tasksystem.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.tasksystem.entity.request.CreateTaskSystemIssueRequest;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.entity.request.TaskSystemLoginRequest;
import com.michal.openai.tasksystem.entity.response.TaskSystemAccessToken;
import com.michal.openai.tasksystem.entity.response.TaskSystemTokenResponse;
import com.michal.openai.tasksystem.entity.token.TaskSystemRefreshToken;
import com.michal.openai.tasksystem.entity.token.TokenStore;
import com.michal.openai.tasksystem.service.impl.TaskSystemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(TaskSystemServiceImpl.class)
@Import(TaskSystemServiceImplTest.TestConfig.class)
class TaskSystemServiceImplTest {
    private final String getAllIssuesEndpoint = "/api/v1/issue/all";
    private final String createIssueEndpoint = "/api/v1/chatgpt/issue/create";
    private final String assignIssueEndpoint = "/api/v1/chatgpt/issue/assign";
    private final String getUserBySlackUserIdEndpoint = "/api/v1/chatgpt/user/slack-user-id";
    private final static String baseUrl = "http://localhost:6901";
    @Autowired MockRestServiceServer server;
    @Autowired ObjectMapper objectMapper;
    @Autowired TaskSystemServiceImpl service;
    @Autowired TokenStore tokenStore;

    @BeforeEach
    void setup() {
        when(tokenStore.isExpired()).thenReturn(true)
                .thenReturn(false);
        when(tokenStore.getAccessToken()).thenReturn("accessToken");
    }

    @Test
    public void shouldCreateTaskSystemIssue() throws JsonProcessingException {
        // given
        var responseJsonFromTaskSystem = "{\\\"title\\\":\\\"Test title\\\",\\\"description\\\":\\\"Test desc\\\",\\\"priority\\\":\\\"HIGH\\\",\\\"authorSlackId\\\":\\\"U08SHTW059C\\\",\\\"assigneeSlackId\\\":\\\"U08SHTW059C\\\",\\\"dueDate\\\":\\\"2025-11-20\\\"}";
        var createIssueRequest = new CreateTaskSystemIssueRequest("Test title", "Test desc", "HIGH", "U08SHTW059C", "U12345678", "2025-11-20", 1);
        var responseIssueDto = new TaskSystemIssueDto("Dummy-1", "Test title", "Test desc", "NEW", "HIGH", "U08SHTW059C", "U12345678");
        var tokenResponse = new TaskSystemTokenResponse(
                new TaskSystemAccessToken("accessToken", LocalDateTime.parse("2026-10-10T00:00:00")),
                new TaskSystemRefreshToken("refreshToken", 1,  LocalDateTime.parse("2026-10-10T00:00:00"), false)
        );

        var loginEndpoint = "/api/v1/login";

        server.expect(requestTo(baseUrl + loginEndpoint))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));

        server.expect(requestTo(baseUrl + createIssueEndpoint))
                .andExpect(header("Authorization", "Bearer accessToken"))
                .andExpect(request -> {
                    String requestBody = request.getBody().toString();
                    CreateTaskSystemIssueRequest requestObj = objectMapper.readValue(requestBody, CreateTaskSystemIssueRequest.class);
                })
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(responseIssueDto), MediaType.APPLICATION_JSON));
        var dtoFromService = service.createIssue(objectMapper.writeValueAsString(createIssueRequest));
        assertThat(dtoFromService).isEqualTo(responseIssueDto);

        server.verify();
    }

    @Test
    void shouldIgnoreUnknownFields() throws Exception {
        // login
        var loginEndpoint = "/api/v1/login";
        var tokenResponse = new TaskSystemTokenResponse(
                new TaskSystemAccessToken("accessToken", LocalDateTime.parse("2026-10-10T00:00:00")),
                new TaskSystemRefreshToken("refreshToken", 1,  LocalDateTime.parse("2026-10-10T00:00:00"), false)
        );

        server.expect(requestTo(baseUrl + loginEndpoint))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));

        var responseIssueDto = new TaskSystemIssueDto("Dummy-1", "Test title", "Test desc", "NEW", "HIGH", "U08SHTW059C", "U12345678");
        String jsonWithExtraFields = """
                {
                  "title": "Test title",
                  "description": "Test desc",
                  "priority": "HIGH",
                  "authorSlackId": "U08SHTW059C",
                  "assigneeSlackId": "U08SHTW059C",
                  "dueDate": "2025-11-20",
                  "extra": "this_should_be_ignored"
                }
                """;

        server.expect(requestTo(baseUrl + createIssueEndpoint))
                .andExpect(header("Authorization", "Bearer accessToken"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseIssueDto),  MediaType.APPLICATION_JSON
            ));
        var dtoFromService = service.createIssue(jsonWithExtraFields);
        assertThat(dtoFromService).isEqualTo(responseIssueDto);
    }

    @Test
    public void shouldThrowCompletionExceptionWhenSendingInvalidJsonToTaskSystem() throws JsonProcessingException {
        // login
        var loginEndpoint = "/api/v1/login";
        var tokenResponse = new TaskSystemTokenResponse(
                new TaskSystemAccessToken("accessToken", LocalDateTime.parse("2026-10-10T00:00:00")),
                new TaskSystemRefreshToken("refreshToken", 1,  LocalDateTime.parse("2026-10-10T00:00:00"), false)
        );

        server.expect(requestTo(baseUrl + loginEndpoint))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));

        String invalidJson = "{ invalidJson";
        // Task-System returns SC 500 for invalid JSON
        server.expect(requestTo(baseUrl + createIssueEndpoint))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer accessToken"))
                .andRespond(withServerError());
        assertThatThrownBy( () -> service.createIssue(invalidJson))
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(JsonParseException.class);
    }

    @Test
    public void shouldThrowExceptionWhenReceivingInvalidIssueDto() throws JsonProcessingException {
        // login
        var loginEndpoint = "/api/v1/login";
        var tokenResponse = new TaskSystemTokenResponse(
                new TaskSystemAccessToken("accessToken", LocalDateTime.parse("2026-10-10T00:00:00")),
                new TaskSystemRefreshToken("refreshToken", 1,  LocalDateTime.parse("2026-10-10T00:00:00"), false)
        );

        server.expect(requestTo(baseUrl + loginEndpoint))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));

        String requestJson = """
        {
          "title": "Test",
          "description": "Desc",
          "priority": "HIGH",
          "authorSlackId": "U1",
          "assigneeSlackId": "U2",
          "dueDate": "2025-11-20"
        }
    """;

        String invalidDtoResponse = """
        { "unknown_field" : "value" }
    """;

        server.expect(requestTo(baseUrl + createIssueEndpoint))
                .andExpect(header("Authorization", "Bearer accessToken"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(invalidDtoResponse,  MediaType.APPLICATION_JSON ));

        var dto = service.createIssue(requestJson);

        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("title", null) // NULL -> new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .hasFieldOrPropertyWithValue("priority", null); // NULL -> new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        server.verify();
    }


    @Test
    public void shouldGetAllIssues() throws JsonProcessingException {
        // login
        var loginEndpoint = "/api/v1/login";
        var tokenResponse = new TaskSystemTokenResponse(
                new TaskSystemAccessToken("accessToken", LocalDateTime.parse("2026-10-10T00:00:00")),
                new TaskSystemRefreshToken("refreshToken", 1,  LocalDateTime.parse("2026-10-10T00:00:00"), false)
        );
        server.expect(requestTo(baseUrl + loginEndpoint))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));

        var issueDto1 = new TaskSystemIssueDto("Dummy-1", "Test title #1", "Test desc", "NEW", "LOW", "U08SHTW059C", "U98765432");
        var issueDto2 = new TaskSystemIssueDto("Dummy-2", "Test title #2", "Test desc", "NEW", "HIGH", "U08SHTW059C", "U98765432");
        List<TaskSystemIssueDto> issueDtoLIst = List.of(issueDto1, issueDto2);
        server.expect(requestTo(baseUrl + getAllIssuesEndpoint))
                .andExpect(header("Authorization", "Bearer accessToken"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(issueDtoLIst), MediaType.APPLICATION_JSON));
        var serviceResponse = service.getAllIssues();
        server.verify();
        assertThat(serviceResponse).isEqualTo(issueDtoLIst);
    }

    private void callLogin() throws JsonProcessingException {
        var request = new TaskSystemLoginRequest("test@test.com", "StrongBotPassword");
        var tokenResponse = new TaskSystemTokenResponse(
                new TaskSystemAccessToken("accessToken", LocalDateTime.parse("2026-10-10T00:00:00")),
                new TaskSystemRefreshToken("refreshToken", 1,  LocalDateTime.parse("2026-10-10T00:00:00"), false)
        );
        var loginEndpoint = "/api/v1/login";

        server.expect(requestTo(baseUrl + loginEndpoint))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnGetIssues() {
        // TODO

    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Qualifier("taskSystemRestClient")
        RestClient  taskSystemRestClient(RestClient.Builder builder) {
            return builder.baseUrl(baseUrl).build();
        }

        @Bean
        TokenStore tokenStore() {
            return org.mockito.Mockito.mock(TokenStore.class);
        }
    }

}
