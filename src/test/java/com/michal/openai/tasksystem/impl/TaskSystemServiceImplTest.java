package com.michal.openai.tasksystem.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.tasksystem.entity.request.CreateTaskSystemIssueRequest;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.service.impl.TaskSystemServiceImpl;
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

import java.util.List;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
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

    @Test
    public void shouldCreateTaskSystemIssue() throws JsonProcessingException {
        // given
        var responseJsonFromTaskSystem = "{\\\"title\\\":\\\"Test title\\\",\\\"description\\\":\\\"Test desc\\\",\\\"priority\\\":\\\"HIGH\\\",\\\"authorSlackId\\\":\\\"U08SHTW059C\\\",\\\"assigneeSlackId\\\":\\\"U08SHTW059C\\\",\\\"dueDate\\\":\\\"2025-11-20\\\"}";
        var createIssueRequest = new CreateTaskSystemIssueRequest("Test title", "Test desc", "HIGH", "U08SHTW059C", "U12345678", "2025-11-20", 1);
        var responseIssueDto = new TaskSystemIssueDto("Dummy-1", "Test title", "Test desc", "NEW", "HIGH", "U08SHTW059C", "U12345678");
        server.expect(requestTo(baseUrl + createIssueEndpoint))
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
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseIssueDto),  MediaType.APPLICATION_JSON
            ));
        var dtoFromService = service.createIssue(jsonWithExtraFields);
        assertThat(dtoFromService).isEqualTo(responseIssueDto);
    }

    @Test
    public void shouldThrowCompletionExceptionWhenSendingInvalidJsonToTaskSystem() {
        String invalidJson = """
                {"title": "Test title",
                 "description": "Test description"}
                """;
        // Task-System returns SC 500 for invalid JSON
        server.expect(requestTo(baseUrl + createIssueEndpoint))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());
        assertThatThrownBy( () -> service.createIssue(invalidJson))
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(HttpServerErrorException.class);
        server.verify();
    }

    @Test
    public void shouldThrowExceptionWhenReceivingInvalidIssueDto() {
        var createIssueRequest = new CreateTaskSystemIssueRequest("Test title", "Test desc", "HIGH", "U08SHTW059C", "U12345678", "2025-11-20", 1);
        var invalidDtoResponse = """
                { "unknown_field" : "value"
                """;
        server.expect(requestTo(baseUrl + createIssueEndpoint))
                .andRespond(withSuccess(invalidDtoResponse,  MediaType.APPLICATION_JSON ));
        assertThatThrownBy( () -> service.createIssue(objectMapper.writeValueAsString(createIssueRequest)))
                .isInstanceOf(CompletionException.class);
        server.verify();
    }

    @Test
    public void shouldAssignIssue() throws JsonProcessingException {
        var issueDto1 = new TaskSystemIssueDto("Dummy-1", "Test title #1", "Test desc", "NEW", "LOW", "U08SHTW059C", "U98765432");
        var issueDto2 = new TaskSystemIssueDto("Dummy-2", "Test title #2", "Test desc", "NEW", "HIGH", "U08SHTW059C", "U98765432");
        List<TaskSystemIssueDto> issueDtoLIst = List.of(issueDto1, issueDto2);
        server.expect(requestTo(baseUrl + getAllIssuesEndpoint))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(issueDtoLIst), MediaType.APPLICATION_JSON));
        var serviceResponse = service.getAllIssues();
        server.verify();
        assertThat(serviceResponse).isEqualTo(issueDtoLIst);
    }

    @Test
    public void shouldReturnGetIssues() {
        //given

    }
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Qualifier("taskSystemRestClient")
        RestClient  taskSystemRestClient(RestClient.Builder builder) {
            return builder.baseUrl(baseUrl).build();
        }
    }
}
