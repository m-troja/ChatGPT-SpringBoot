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
    import com.michal.openai.persistence.JpaGptMessageRepo;
    import com.michal.openai.persistence.JpaGptRequestRepo;
    import com.michal.openai.persistence.JpaGptResponseRepo;
    import com.michal.openai.persistence.JpaSlackRepo;
    import com.michal.openai.slack.entity.SlackUser;
    import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;
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
    import org.springframework.http.MediaType;
    import org.springframework.test.context.TestPropertySource;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.client.MockRestServiceServer;
    import org.springframework.test.web.client.RequestMatcher;
    import org.springframework.web.client.RestClient;

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

        @MockitoBean JpaGptRequestRepo jpaGptRequestRepo;
        @MockitoBean JpaGptResponseRepo jpaGptResponseRepo;
        @MockitoBean JpaSlackRepo jpaSlackRepo;
        @MockitoBean JpaGptMessageRepo messageRepo; // needed for service constructor
        @MockitoBean FunctionFacory functionFactory;
        @MockitoBean AssignTaskSystemIssueFunction assignTaskSystemIssueFunction;
        @MockitoBean CreateTaskSystemIssueFunction createTaskSystemIssueFunction;

        @BeforeEach
        void setup() {
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
            SlackUser user = new SlackUser("U12345678", "Slack Name");
            when(jpaSlackRepo.findBySlackUserId("U12345678")).thenReturn(user);
            when(jpaGptRequestRepo.getLastRequestsBySlackId("U12345678", 5)).thenReturn(List.of());
            when(jpaGptResponseRepo.getLastResponsesToUser("U12345678", 5)).thenReturn(List.of());
            when(jpaGptRequestRepo.save(any())).thenAnswer(inv -> {
                GptRequest req = inv.getArgument(0);
                req.setId(1L);
                return req;
            });
            var gptResponse = buildResponse("Hi there!", null);

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

            assertThat(answerFromService).isEqualTo("Hi there!");
        }

        @Test
        void shouldThrowExceptionWhenInvalidGptResponse() {
            SlackUser user = new SlackUser("U12345678", "Slack Name");
            when(jpaSlackRepo.findBySlackUserId("U12345678")).thenReturn(user);
            when(jpaGptRequestRepo.getLastRequestsBySlackId("U12345678", 5)).thenReturn(List.of());
            when(jpaGptResponseRepo.getLastResponsesToUser("U12345678", 5)).thenReturn(List.of());
            when(jpaGptRequestRepo.save(any())).thenAnswer(inv -> {
                GptRequest req = inv.getArgument(0);
                req.setId(1L);
                return req;
            });
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
            var expectedDtoJson = objectMapper.writeValueAsString(new TaskSystemIssueDto("Dummy-1", "Title", "Desc", "NEW", "HIGH", "U12345678", "U12345678"));
            SlackUser slackRequestAuthor = new SlackUser(slackUserId, "Slack Name");
            CompletableFuture<String> query = CompletableFuture.completedFuture("Assign ticket Dummy-1 to U12345678");
            CompletableFuture<String> userName = CompletableFuture.completedFuture("U12345678");
            // when
            when(jpaSlackRepo.findBySlackUserId(slackUserId)).thenReturn(slackRequestAuthor);
            when(jpaGptRequestRepo.getLastRequestsBySlackId(slackUserId, service.getQtyContextMessagesInRequestOrResponse())).thenReturn(List.of("Test request 1", "Test request 2"));
            when(jpaGptResponseRepo.getLastResponsesToUser(slackUserId, service.getQtyContextMessagesInRequestOrResponse())).thenReturn(List.of("Test response 1", "Test response 2"));
            when(jpaGptRequestRepo.save(any())).thenAnswer(inv -> {
                GptRequest req = inv.getArgument(0);
                req.setId(1L);
                return req;
            });
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
            assertThat(gptResponseFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().name()).isEqualTo("assignTaskSystemIssueFunction");
            assertThat(gptResponseFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().arguments()).isEqualTo("{\\\"key\\\":\\\"Dummy-1\\\",\\\"slackUserId\\\":\\\"U12345678\\\"}");

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
            var newIssueDtoJson = objectMapper.writeValueAsString(new TaskSystemIssueDto("Dummy-1", "Test title", "Test desc", "NEW", "HIGH", "U08SHTW059C", "U08SHTW059C"));
            var reassignedIssueDtoJson = objectMapper.writeValueAsString(new TaskSystemIssueDto("Dummy-1", "Test title", "Test desc", "NEW", "HIGH", "U08SHTW059C", "U12345678"));
            var finalResponseContent = "Your issue key is Dummy-1, it was assigned to U12345678";

            System.out.println("newIssueDtoJson: " + newIssueDtoJson);
            System.out.println("reassignedIssueDtoJson: " + reassignedIssueDtoJson);

            SlackUser slackRequestAuthor = new SlackUser(slackUserId, "Slack Name");
            CompletableFuture<String> query = CompletableFuture.completedFuture(queryFuture);
            CompletableFuture<String> userName = CompletableFuture.completedFuture(slackUserId);

            // when
            when(jpaSlackRepo.findBySlackUserId(slackUserId)).thenReturn(slackRequestAuthor);
            when(jpaGptRequestRepo.getLastRequestsBySlackId(slackUserId, service.getQtyContextMessagesInRequestOrResponse())).thenReturn(List.of("Test request 1", "Test request 2"));
            when(jpaGptResponseRepo.getLastResponsesToUser(slackUserId, service.getQtyContextMessagesInRequestOrResponse())).thenReturn(List.of("Test response 1", "Test response 2"));
            when(jpaGptRequestRepo.save(any())).thenAnswer(inv -> {
                GptRequest req = inv.getArgument(0);
                req.setId(1L);
                return req;
            });
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
            assertThat(gptResponseCreateIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().name()).isEqualTo("createTaskSystemIssueFunction");
            assertThat(gptResponseCreateIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().arguments()).isEqualTo("{\\\"title\\\":\\\"Test title\\\",\\\"description\\\":\\\"Test desc\\\",\\\"priority\\\":\\\"HIGH\\\",\\\"authorSlackId\\\":\\\"U08SHTW059C\\\",\\\"assigneeSlackId\\\":\\\"U08SHTW059C\\\",\\\"dueDate\\\":\\\"2025-11-20\\\"}");


            // 2nd response - check if GPT sent assignTaskSystemIssueFunction
            Assertions.assertNotNull(gptResponseAssignIssueFunctionCall);
            assertThat(gptResponseAssignIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls()).isNotNull();
            assertThat(gptResponseAssignIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().name()).isEqualTo("assignTaskSystemIssueFunction");
            assertThat(gptResponseAssignIssueFunctionCall.getChoices().getFirst().getMessage().getToolCalls().getFirst().getFunctionCall().arguments()).isEqualTo("{\\\"key\\\":\\\"Dummy-1\\\",\\\"slackUserId\\\":\\\"U12345678\\\"}");

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
                GptRequest gptRequest = objectMapper.readValue(json, GptRequest.class);
                assertThat(gptRequest.getMessages()).isNotNull();
                //  check number of messages sent in context
                System.out.println("Size of message list: " + gptRequest.getMessages().size());
                gptRequest.getMessages().forEach(m -> System.out.println("Found GptMessage: " + m.toString()));
                assertThat(gptRequest.getMessages().size()).isGreaterThanOrEqualTo(totalQtyMessagesInContext);
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
                var tool = new GptMessage.Tool("id", "function");
                var functionCall = new GptMessage.Tool.FunctionCall("assignTaskSystemIssueFunction", "{\\\"key\\\":\\\"Dummy-1\\\",\\\"slackUserId\\\":\\\"U12345678\\\"}");
                tool.setFunctionCall(functionCall);
                message.setToolCalls(List.of(tool));
                choice.setMessage(message);
                response.setChoices(List.of(choice));
                return response;
            } else if (functionName.equals("createTaskSystemIssueFunction")) {
                assertThat(content).isNull();
                var tool = new GptMessage.Tool("id", "function");
                var functionCall = new GptMessage.Tool.FunctionCall("createTaskSystemIssueFunction", "{\\\"title\\\":\\\"Test title\\\",\\\"description\\\":\\\"Test desc\\\",\\\"priority\\\":\\\"HIGH\\\",\\\"authorSlackId\\\":\\\"U08SHTW059C\\\",\\\"assigneeSlackId\\\":\\\"U08SHTW059C\\\",\\\"dueDate\\\":\\\"2025-11-20\\\"}");
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
    }