    package com.michal.openai.gpt.impl;

    import com.fasterxml.jackson.core.JsonProcessingException;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.michal.openai.config.BeansConfiguration;
    import com.michal.openai.functions.FunctionFacory;
    import com.michal.openai.functions.impl.AssignTaskSystemIssueFunction;
    import com.michal.openai.functions.impl.CreateTaskSystemIssueFunction;
    import com.michal.openai.gpt.entity.GptMessage;
    import com.michal.openai.gpt.entity.GptRequest;
    import com.michal.openai.gpt.entity.GptResponse;
    import com.michal.openai.persistence.RequestDtoRepo;
    import com.michal.openai.persistence.ResponseDtoRepo;
    import com.michal.openai.persistence.SlackRepo;
    import com.michal.openai.slack.entity.SlackUser;
    import com.michal.openai.tasksystem.entity.dto.TaskSystemCommentDto;
    import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
    import org.hamcrest.Matchers;
    import org.junit.jupiter.api.Assertions;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
    import org.springframework.boot.test.context.TestConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Import;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.MediaType;
    import org.springframework.test.context.TestPropertySource;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.client.MockRestServiceServer;
    import org.springframework.test.web.client.RequestMatcher;
    import org.springframework.web.client.RestClient;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


    import java.time.OffsetDateTime;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.concurrent.CompletableFuture;
    import java.util.concurrent.CompletionException;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.assertj.core.api.Assertions.assertThatThrownBy;
    import static org.mockito.Mockito.*;
    import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
    import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
    import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

    @RestClientTest(GptServiceImpl.class)
    @Import(BeansConfiguration.class)
    @TestPropertySource(properties = {
            "CHAT_MAX_TOKENS=100",
            "gpt.function.tasksystem.assign.issue.desc",
            "gpt.function.tasksystem.assign.issue.name",
            "gpt.function.tasksystem.assign.issue.attr.key.desc",
            "gpt.function.tasksystem.assign.issue.attr.slackUserId.desc"
            })
    class GptServiceImplTest {

        private static final int qtyContextMessagesInRequestOrResponse = 2;
        private static final int totalQtyMessagesInContext = qtyContextMessagesInRequestOrResponse * 2 + 2; // Context + System/Initial Msg + query
        private static final String chatGptApiUrl = "https://api.openai.com/v1/chat/completions";
        @Autowired MockRestServiceServer server;
        @Autowired ObjectMapper objectMapper;
        @Autowired GptServiceImpl service;

        @MockitoBean ResponseDtoRepo responseDtoRepo;
        @MockitoBean SlackRepo slackRepo;
        @MockitoBean RequestDtoRepo requestDtoRepo;
        @MockitoBean FunctionFacory functionFactory;
        @MockitoBean AssignTaskSystemIssueFunction assignTaskSystemIssueFunction;
        @MockitoBean CreateTaskSystemIssueFunction createTaskSystemIssueFunction;

        @BeforeEach
        void setup() {
            objectMapper.registerModule(new JavaTimeModule());
            service.setQtyContextMessagesInRequestOrResponse(qtyContextMessagesInRequestOrResponse);
            service.setModel("gpt-5.1");
            service.setTemperature(0.8);
            service.setRetryAttempts(3);
            service.setWaitSeconds(0);
            service.setMaxTokens(100);
            service.setSystemInitialMessage("Hi");
        }

        @Test
        void shouldAnswerNoFunctionCall() throws JsonProcessingException {
            var expectedAnswerFromService = "Hello from gpts";
            SlackUser user = new SlackUser("U12345678", "Slack Name");
            when(slackRepo.findBySlackUserId("U12345678")).thenReturn(user);

            Pageable limit = PageRequest.of(0, qtyContextMessagesInRequestOrResponse);
            when(requestDtoRepo.findByUserSlackIdOrderByTimestampDesc("U12345678", limit)).thenReturn(List.of());
            when(responseDtoRepo.findByUserSlackIdOrderByTimestampDesc("U12345678", limit)).thenReturn(List.of());

            var gptResponse = buildResponse(expectedAnswerFromService, null);

            // mock GptResponse
            server.expect(requestTo(chatGptApiUrl))
                    .andExpect(request -> {
                        String json = request.getBody().toString();
                        GptRequest req = objectMapper.readValue(json, GptRequest.class);
                        assertThat(req.getMessages()).isNotEmpty();
                    })
                    .andRespond(withSuccess(objectMapper.writeValueAsString(gptResponse), MediaType.APPLICATION_JSON));


            CompletableFuture<String> query = CompletableFuture.completedFuture("Just say hi");
            CompletableFuture<String> userName = CompletableFuture.completedFuture(user.getSlackUserId());

            String answerFromService = service.getAnswerWithSlack(query, userName).join();
            server.verify();

            assertThat(answerFromService).isEqualTo(expectedAnswerFromService);
        }

        @Test
        void shouldThrowExceptionWhenInvalidGptResponse() {
            SlackUser user = new SlackUser("U12345678", "Slack Name");
            when(slackRepo.findBySlackUserId("U12345678")).thenReturn(user);
            Pageable limit = PageRequest.of(0, qtyContextMessagesInRequestOrResponse);
            when(requestDtoRepo.findByUserSlackIdOrderByTimestampDesc("U12345678", limit)).thenReturn(List.of());
            when(responseDtoRepo.findByUserSlackIdOrderByTimestampDesc("U12345678", limit)).thenReturn(List.of());

            String invalidJson = """
                    {"key":"value"}
                    """;
            // mock GptResponse
            server.expect(requestTo(chatGptApiUrl))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(Matchers.containsString("messages")))
                    .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));
            server.expect(requestTo(chatGptApiUrl))
                    .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

            server.expect(requestTo(chatGptApiUrl))
                    .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));


            CompletableFuture<String> query = CompletableFuture.completedFuture("Just say hi");
            CompletableFuture<String> userName = CompletableFuture.completedFuture(user.getSlackUserId());

            assertThatThrownBy(() -> service.getAnswerWithSlack(query, userName).join()).isInstanceOf(CompletionException.class);
            server.verify();
        }

        @Test
        void shouldCallTaskSystemAssignIssueFunctionTest() throws JsonProcessingException {
            // given
            var slackUserId = "U12345678";
            var expectedDtoJson = objectMapper.writeValueAsString(getIssueDto());
            SlackUser slackRequestAuthor = new SlackUser(slackUserId, "Slack Name");
            CompletableFuture<String> query = CompletableFuture.completedFuture("Assign ticket Dummy-1 to U12345678");
            CompletableFuture<String> userName = CompletableFuture.completedFuture("U12345678");
            // when
            when(slackRepo.findBySlackUserId(slackUserId)).thenReturn(slackRequestAuthor);
            Pageable limit = PageRequest.of(0, qtyContextMessagesInRequestOrResponse);
            when(requestDtoRepo.findByUserSlackIdOrderByTimestampDesc("U12345678", limit)).thenReturn(List.of());
            when(responseDtoRepo.findByUserSlackIdOrderByTimestampDesc("U12345678", limit)).thenReturn(List.of());

            when(functionFactory.getFunctionByFunctionName("assignTaskSystemIssueFunction")).thenReturn(assignTaskSystemIssueFunction);
            when(assignTaskSystemIssueFunction.execute(anyString())).thenReturn("Response from function");

            // then
            var gptResponseFunctionCall = buildResponse(null, "assignTaskSystemIssueFunction");
            var gptResponseNoFunctionCall = buildResponse(expectedDtoJson, null);

            // mock GptResponse - function call
            server.expect(requestTo(chatGptApiUrl))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(captureAndAssertGptRequest(objectMapper))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(gptResponseFunctionCall), MediaType.APPLICATION_JSON));

            // mock GptResponse - no function call
            server.expect(requestTo(chatGptApiUrl))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(captureAndAssertGptRequest(objectMapper))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(gptResponseNoFunctionCall), MediaType.APPLICATION_JSON));
            String response = service.getAnswerWithSlack(query, userName).join(); // needed to call service
            server.verify();

            // 1st response - check if GPT sent Function Call
            Assertions.assertNotNull(gptResponseFunctionCall);
            assertThat(gptResponseFunctionCall.getChoices().getFirst().getMessage().getToolCalls()).isNotNull();
            assertThat(gptResponseFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().getName()).isEqualTo("assignTaskSystemIssueFunction");
            assertThat(gptResponseFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().getArguments()).isEqualTo("{\\\"key\\\":\\\"Dummy-1\\\",\\\"slackUserId\\\":\\\"U12345678\\\"}");

            // 2nd response - check if GPT did not send Function Call
            Assertions.assertNotNull(gptResponseNoFunctionCall);
            assertThat(gptResponseNoFunctionCall.getChoices().getFirst().getMessage().getToolCalls()).isNull();
            assertThat(gptResponseNoFunctionCall.getChoices().getFirst().getMessage().getContent()).isEqualTo(expectedDtoJson);

            // Check if GPT built context if totalQtyMessagesInContext
        }

        @Test
        public void shouldReturnTwoFunctionCalls() throws JsonProcessingException {
            // given
            var slackUserId = "U08SHTW059C";
            var queryFuture = "Create Task-system issue: title= Test title, description= Test desc, Due date: 14 days, priority: HIGH, author=U08SHTW059C,  assignee: U08SHTW059C. Then after that, assign returned issue to this issue to U08RQ4PPVNW.";
            var newIssueDtoJson = objectMapper.writeValueAsString(getIssueDto());
            var reassignedIssueDtoJson = objectMapper.writeValueAsString(getIssueDto());
            var finalResponseContent = "Your issue key is Dummy-1, it was assigned to U12345678";

            System.out.println("newIssueDtoJson: " + newIssueDtoJson);
            System.out.println("reassignedIssueDtoJson: " + reassignedIssueDtoJson);

            SlackUser slackRequestAuthor = new SlackUser(slackUserId, "Slack Name");
            CompletableFuture<String> query = CompletableFuture.completedFuture(queryFuture);
            CompletableFuture<String> userName = CompletableFuture.completedFuture(slackUserId);

            // when
            when(slackRepo.findBySlackUserId(slackUserId)).thenReturn(slackRequestAuthor);
            Pageable limit = PageRequest.of(0, qtyContextMessagesInRequestOrResponse);
            when(requestDtoRepo.findByUserSlackIdOrderByTimestampDesc("U12345678", limit)).thenReturn(List.of());
            when(responseDtoRepo.findByUserSlackIdOrderByTimestampDesc("U12345678", limit)).thenReturn(List.of());

            when(functionFactory.getFunctionByFunctionName("createTaskSystemIssueFunction")).thenReturn(createTaskSystemIssueFunction);
            when(assignTaskSystemIssueFunction.execute(anyString())).thenReturn(newIssueDtoJson);
            when(functionFactory.getFunctionByFunctionName("assignTaskSystemIssueFunction")).thenReturn(assignTaskSystemIssueFunction);
            when(assignTaskSystemIssueFunction.execute(anyString())).thenReturn(reassignedIssueDtoJson);

            var gptResponseCreateIssueFunctionCall = buildResponse(null, "createTaskSystemIssueFunction");
            var gptResponseAssignIssueFunctionCall = buildResponse(null, "assignTaskSystemIssueFunction");
            var gptResponseFinal = buildResponse(finalResponseContent, null);

            //then
            server.expect(requestTo(chatGptApiUrl))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(captureAndAssertGptRequest(objectMapper))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(gptResponseCreateIssueFunctionCall), MediaType.APPLICATION_JSON));
            server.expect(requestTo(chatGptApiUrl))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(captureAndAssertGptRequest(objectMapper))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(gptResponseAssignIssueFunctionCall), MediaType.APPLICATION_JSON));
            server.expect(requestTo(chatGptApiUrl))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(captureAndAssertGptRequest(objectMapper))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(gptResponseFinal), MediaType.APPLICATION_JSON));
            String serviceResponse = service.getAnswerWithSlack(query, userName).join();
            server.verify();

            // 1st response - check if GPT sent createTaskSystemIssueFunction
            Assertions.assertNotNull(gptResponseCreateIssueFunctionCall);
            assertThat(gptResponseCreateIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls()).isNotNull();
            assertThat(gptResponseCreateIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().getName()).isEqualTo("createTaskSystemIssueFunction");
            assertThat(gptResponseCreateIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().getArguments()).isEqualTo("{\\\"title\\\":\\\"Test title\\\",\\\"description\\\":\\\"Test desc\\\",\\\"priority\\\":\\\"HIGH\\\",\\\"authorSlackId\\\":\\\"U08SHTW059C\\\",\\\"assigneeSlackId\\\":\\\"U08SHTW059C\\\",\\\"dueDate\\\":\\\"2025-11-20\\\"}");


            // 2nd response - check if GPT sent assignTaskSystemIssueFunction
            Assertions.assertNotNull(gptResponseAssignIssueFunctionCall);
            assertThat(gptResponseAssignIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls()).isNotNull();
            assertThat(gptResponseAssignIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().getName()).isEqualTo("assignTaskSystemIssueFunction");
            assertThat(gptResponseAssignIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().getArguments()).isEqualTo("{\\\"key\\\":\\\"Dummy-1\\\",\\\"slackUserId\\\":\\\"U12345678\\\"}");

            // 3rd response - check if GPT did not send Function Call
            Assertions.assertNotNull(gptResponseFinal);
            assertThat(gptResponseFinal.getChoices().getFirst().getMessage().getToolCalls()).isNull();
            assertThat(gptResponseFinal.getChoices().getFirst().getMessage().getContent()).isEqualTo(finalResponseContent);

            assertThat(serviceResponse).isEqualTo(finalResponseContent);
        }

        private RequestMatcher captureAndAssertGptRequest(ObjectMapper objectMapper) {
            return request -> {
                System.out.println("Intercepting GptRequest...");
                String json = request.getBody().toString();
                objectMapper.registerModule(new JavaTimeModule());
                GptRequest gptRequest = objectMapper.readValue(json, GptRequest.class);
                assertThat(gptRequest.getMessages()).isNotNull();
                //  check number of messages sent in context
                System.out.println("Size of message list: " + gptRequest.getMessages().size());
                gptRequest.getMessages().forEach(m -> System.out.println("Found GptMessage: " + m.toString()));
                assertThat(gptRequest.getMessages().size()).isLessThanOrEqualTo(totalQtyMessagesInContext);
            };
        }

        // Utils

        private GptResponse buildResponse(String content, String functionName) {
            var response = new GptResponse();
            var choice = new GptResponse.Choice();
            var message = new GptMessage("assistant", content);
            if (functionName == null) {
                assertThat(content).isNotNull();
                choice.setMessage(message);
                response.setChoices(List.of(choice));
                return response;
            } else if (functionName.equals("assignTaskSystemIssueFunction")) {
                assertThat(content).isNull();
                var tool = new GptMessage.ToolCall("id", "function");
                var functionCall = new GptMessage.ToolCall.FunctionCall("assignTaskSystemIssueFunction", "{\\\"key\\\":\\\"Dummy-1\\\",\\\"slackUserId\\\":\\\"U12345678\\\"}");
                tool.setFunctionCall(functionCall);
                message.setToolCalls(List.of(tool));
                choice.setMessage(message);
                response.setChoices(List.of(choice));
                return response;
            } else if (functionName.equals("createTaskSystemIssueFunction")) {
                assertThat(content).isNull();
                var tool = new GptMessage.ToolCall("id", "function");
                var functionCall = new GptMessage.ToolCall.FunctionCall("createTaskSystemIssueFunction", "{\\\"title\\\":\\\"Test title\\\",\\\"description\\\":\\\"Test desc\\\",\\\"priority\\\":\\\"HIGH\\\",\\\"authorSlackId\\\":\\\"U08SHTW059C\\\",\\\"assigneeSlackId\\\":\\\"U08SHTW059C\\\",\\\"dueDate\\\":\\\"2025-11-20\\\"}");
                tool.setFunctionCall(functionCall);
                message.setToolCalls(List.of(tool));
                choice.setMessage(message);
                response.setChoices(List.of(choice));
                return response;
            }
            return null;
        }


        @TestConfiguration
        static class TestConfig {
            @Bean
            @Qualifier("gptRestClient")
            RestClient gptRestClient(RestClient.Builder builder) {
                return builder.baseUrl(chatGptApiUrl).build();
            }
        }

        private TaskSystemIssueDto getIssueDto() {
            var commentDto = new TaskSystemCommentDto(1, 1, "conent", 1, OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"), "authorName");
            List<TaskSystemCommentDto> comments = new ArrayList<>();
            comments.add(commentDto);
            return new TaskSystemIssueDto(1, "Dummy-1", "Title", "Desc", "NEW", "HIGH", "U12345678", "U12345677",
                    OffsetDateTime.parse("2025-09-15T19:32:24Z") , OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"),
                    comments,
                    1
            );
        }
    }