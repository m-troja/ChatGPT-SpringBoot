package com.michal.openai.tasksystem.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.persistence.SlackRepo;
import com.michal.openai.slack.SlackService;
import com.michal.openai.slack.entity.SlackUser;
import com.michal.openai.tasksystem.entity.TaskSystemEventType;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.entity.dto.TaskSystemUserDto;
import com.michal.openai.tasksystem.entity.response.TaskSystemEvent;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(TaskSystemServiceImpl.class)
@Import(TaskSystemServiceImplTest.TestConfig.class)
class TaskSystemServiceImplTest {

    private final static String baseUrl = "http://localhost:6901";

    @Autowired MockRestServiceServer server;
    @Autowired ObjectMapper objectMapper;
    @Autowired TaskSystemServiceImpl service;
    @MockitoBean TokenStore tokenStore;
    @MockitoBean SlackService slackService;
    @MockitoBean SlackRepo slackRepo;

    SlackUser slackUser;

    @BeforeEach
    void setup() {

        slackUser = new SlackUser();
        slackUser.setSlackUserId("U123");

        when(tokenStore.isExpired()).thenReturn(false);
        when(tokenStore.getAccessToken()).thenReturn("token");
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private TaskSystemIssueDto issue() {

        return new TaskSystemIssueDto(
                1,
                "TS-1",
                "Test issue",
                "Platform",
                "description",
                "OPEN",
                "HIGH",
                "U111",
                "U222",
                OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(2),
                OffsetDateTime.now(),
                List.of(),
                1
        );
    }

    private List<TaskSystemIssueDto> issues() {
        return List.of(issue());
    }

    @Test
    void shouldCreateIssueSuccessfully() throws Exception {

        server.expect(anything())
                .andRespond(withSuccess(json(issue()), MediaType.APPLICATION_JSON));

        var result = service.createIssue(mock());

        assertThat(result.key()).isEqualTo("TS-1");

        server.verify();
    }

    @Test
    void shouldThrowWhenCreateIssueFails() {

        server.expect(anything())
                .andRespond(request -> { throw new RuntimeException(); });

        assertThatThrownBy(() -> service.createIssue(mock()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldAssignIssueSuccessfully() throws Exception {

        server.expect(anything())
                .andRespond(withSuccess(json(issue()), MediaType.APPLICATION_JSON));

        var result = service.assignIssue(mock());

        assertThat(result.title()).isEqualTo("Test issue");
    }

    @Test
    void shouldThrowWhenAssignIssueFails() {

        server.expect(anything())
                .andRespond(request -> { throw new RuntimeException(); });

        assertThatThrownBy(() -> service.assignIssue(mock()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldReturnAllIssues() throws Exception {

        server.expect(anything())
                .andRespond(withSuccess(json(issues()), MediaType.APPLICATION_JSON));

        var result = service.getAllIssues();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldThrowWhenGetAllIssuesFails() {

        server.expect(anything())
                .andRespond(request -> { throw new RuntimeException(); });

        assertThatThrownBy(service::getAllIssues)
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldReturnIssuesForUser() throws Exception {

        server.expect(anything())
                .andRespond(withSuccess(json(issues()), MediaType.APPLICATION_JSON));

        var result = service.getIssuesForUser("U123");

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldThrowWhenGetIssuesForUserFails() {

        server.expect(anything())
                .andRespond(request -> { throw new RuntimeException(); });

        assertThatThrownBy(() -> service.getIssuesForUser("U123"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFetchUserSuccessfully() throws Exception {
        var user = new TaskSystemUserDto(
                1,
                "Bot",
                "Name",
                "bot@example.com",
                List.of("ROLE_USER"),
                List.of("TeamA"),
                false,
                "U123"
        );

        server.expect(anything())
                .andRespond(withSuccess(json(user), MediaType.APPLICATION_JSON));

        var result = service.getTaskSystemUser("U123");

        assertThat(result).isNotNull();
        assertThat(result.userSlackId()).isEqualTo("U123");
        assertThat(result.email()).isEqualTo("bot@example.com");
    }

    @Test
    void shouldThrowUnauthorizedWhenFetchingUser() {

        server.expect(anything())
                .andRespond(request -> {

                    throw HttpClientErrorException.create(
                            HttpStatus.UNAUTHORIZED,
                            "Unauthorized",
                            null,
                            null,
                            null
                    );
                });

        assertThatThrownBy(() -> service.getTaskSystemUser("U123"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowGenericErrorWhenFetchingUser() {

        server.expect(anything())
                .andRespond(request -> { throw new RuntimeException(); });

        assertThatThrownBy(() -> service.getTaskSystemUser("U123"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldSendSlackMessageOnCreatedIssue() {

        var event = mock(TaskSystemEvent.class);
        var issueDto = issue();

        when(event.event()).thenReturn(TaskSystemEventType.CREATED_ISSUE);
        when(event.issue()).thenReturn(issueDto);
        when(event.eventUserSlackId()).thenReturn("U999");

        when(slackRepo.findBySlackUserId(issueDto.assigneeSlackId())).thenReturn(slackUser);
        when(slackRepo.findBySlackUserId(issueDto.authorSlackId())).thenReturn(slackUser);

        service.parseTaskSystemEvent(event);

        verify(slackService).sendMessageToSlack(anyString(), anyString());
    }

    @Test
    void shouldSendSlackMessageOnUpdatedAssignee() {

        var event = mock(TaskSystemEvent.class);
        var issueDto = issue();

        when(event.event()).thenReturn(TaskSystemEventType.UPDATED_ASSIGNEE);
        when(event.issue()).thenReturn(issueDto);
        when(event.eventUserSlackId()).thenReturn("U999");

        when(slackRepo.findBySlackUserId(issueDto.assigneeSlackId())).thenReturn(slackUser);
        when(slackRepo.findBySlackUserId(issueDto.authorSlackId())).thenReturn(slackUser);

        service.parseTaskSystemEvent(event);

        verify(slackService).sendMessageToSlack(anyString(), anyString());
    }

    @Test
    void shouldSendSlackMessageOnDeletedIssue() {

        var event = mock(TaskSystemEvent.class);
        when(event.event()).thenReturn(TaskSystemEventType.DELETED_ISSUE);
        var issueDto = issue();

        when(event.issue()).thenReturn(issueDto);
        when(event.eventUserSlackId()).thenReturn("U999");

        when(slackRepo.findBySlackUserId(issueDto.assigneeSlackId())).thenReturn(slackUser);
        when(slackRepo.findBySlackUserId(issueDto.authorSlackId())).thenReturn(slackUser);

        service.parseTaskSystemEvent(event);

        verify(slackService).sendMessageToSlack(anyString(), anyString());
    }

    @Test
    void shouldSendSlackMessageOnCreatedComment() {

        var event = mock(TaskSystemEvent.class);
        when(event.event()).thenReturn(TaskSystemEventType.CREATED_COMMENT);
        var issueDto = issue();

        when(event.issue()).thenReturn(issueDto);
        when(event.eventUserSlackId()).thenReturn("U999");

        when(slackRepo.findBySlackUserId(issueDto.assigneeSlackId())).thenReturn(slackUser);
        when(slackRepo.findBySlackUserId(issueDto.authorSlackId())).thenReturn(slackUser);

        service.parseTaskSystemEvent(event);

        verify(slackService).sendMessageToSlack(anyString(), anyString());
    }

    @Test
    void shouldSendSlackMessageOnPriorityUpdate() {

        var event = mock(TaskSystemEvent.class);
        when(event.event()).thenReturn(TaskSystemEventType.UPDATED_PRIORITY);

        var issueDto = issue();

        when(event.issue()).thenReturn(issueDto);
        when(event.eventUserSlackId()).thenReturn("U999");

        when(slackRepo.findBySlackUserId(issueDto.assigneeSlackId())).thenReturn(slackUser);
        when(slackRepo.findBySlackUserId(issueDto.authorSlackId())).thenReturn(slackUser);

        service.parseTaskSystemEvent(event);

        verify(slackService).sendMessageToSlack(anyString(), anyString());
    }

    @Test
    void shouldSendSlackMessageOnStatusUpdate() {

        var event = mock(TaskSystemEvent.class);
        when(event.event()).thenReturn(TaskSystemEventType.UPDATED_STATUS);

        var issueDto = issue();

        when(event.issue()).thenReturn(issueDto);
        when(event.eventUserSlackId()).thenReturn("U999");

        when(slackRepo.findBySlackUserId(issueDto.assigneeSlackId())).thenReturn(slackUser);
        when(slackRepo.findBySlackUserId(issueDto.authorSlackId())).thenReturn(slackUser);


        service.parseTaskSystemEvent(event);

        verify(slackService).sendMessageToSlack(anyString(), anyString());
    }

    @Test
    void shouldThrowWhenEventTypeInvalid() {

        var event = mock(TaskSystemEvent.class);
        when(event.event()).thenReturn(null);

        assertThatThrownBy(() -> service.parseTaskSystemEvent(event))
                .isInstanceOf(IllegalArgumentException.class);
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