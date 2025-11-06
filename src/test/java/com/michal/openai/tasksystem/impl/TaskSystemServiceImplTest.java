package com.michal.openai.tasksystem.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.tasksystem.entity.request.CreateTaskSystemIssueRequest;
import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;
import com.michal.openai.tasksystem.service.impl.TaskSystemServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(TaskSystemServiceImpl.class)
@Import(TaskSystemServiceImplTest.TestConfig.class)
class TaskSystemServiceImplTest {
    private String getAllIssuesEndpoint = "/api/v1/issue/all";
    private String createIssueEndpoint = "/api/v1/chatgpt/issue/create";
    private String assignIssueEndpoint = "/api/v1/chatgpt/issue/assign";
    private String getUserBySlackUserIdEndpoint = "/api/v1/chatgpt/user/slack-user-id";
    private static String baseUrl = "http://localhost:6901";

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
                .andRespond(withSuccess(objectMapper.writeValueAsString(responseIssueDto), MediaType.APPLICATION_JSON));
        var dtoFromService = service.createIssue(objectMapper.writeValueAsString(createIssueRequest));
        assertThat(dtoFromService).isEqualTo(responseIssueDto);

        server.verify();
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
