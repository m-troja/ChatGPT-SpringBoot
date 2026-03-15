    package com.michal.openai.gpt.impl;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
    import com.michal.openai.config.BeansConfiguration;
    import com.michal.openai.gpt.entity.GptMessage;
    import com.michal.openai.gpt.entity.GptResponse;
    import com.michal.openai.gpt.service.impl.GptServiceImpl;
    import com.michal.openai.persistence.RequestDtoRepo;
    import com.michal.openai.persistence.ResponseDtoRepo;
    import com.michal.openai.persistence.SlackRepo;
    import com.michal.openai.tasksystem.entity.dto.TaskSystemCommentDto;
    import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
    import org.junit.jupiter.api.BeforeEach;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
    import org.springframework.boot.test.context.TestConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Import;
    import org.springframework.test.context.TestPropertySource;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.client.MockRestServiceServer;
    import org.springframework.web.client.RestClient;

    import java.time.OffsetDateTime;
    import java.util.ArrayList;
    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;

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
            List<Integer> attachmentIds = new ArrayList<>();
            attachmentIds.add(1);
            var commentDto = new TaskSystemCommentDto(1, 1, "conent", 1, OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"), "authorName",
                    attachmentIds, "slackUserId");
            List<TaskSystemCommentDto> comments = new ArrayList<>();
            comments.add(commentDto);
            return new TaskSystemIssueDto(1, "Dummy-1", "Title", "Desc", "NEW", "HIGH", "U12345678", "U12345677", "U12345678",
                    OffsetDateTime.parse("2025-09-15T19:32:24Z") , OffsetDateTime.parse("2025-09-15T19:32:24Z"), OffsetDateTime.parse("2025-09-15T19:32:24Z"),
                    comments,
                    1
            );
        }
    }