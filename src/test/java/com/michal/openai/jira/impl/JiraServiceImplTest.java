    package com.michal.openai.jira.impl;

    import com.fasterxml.jackson.core.JsonProcessingException;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.michal.openai.exception.JiraCommunicationException;
    import com.michal.openai.functions.FunctionFacory;
    import com.michal.openai.functions.entity.GptFunction;
    import com.michal.openai.functions.impl.CreateJiraIssueFunction;
    import com.michal.openai.functions.impl.GetAllJiraIssuesFunction;
    import com.michal.openai.jira.entity.*;
    import com.michal.openai.jira.service.impl.JiraServiceImpl;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
    import org.springframework.boot.test.context.TestConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Import;
    import org.springframework.http.HttpMethod;
    import org.springframework.http.MediaType;
    import org.springframework.test.context.TestPropertySource;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.client.MockRestServiceServer;
    import org.springframework.web.client.RestClient;

    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.assertj.core.api.Assertions.assertThatThrownBy;
    import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
    import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
    import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
    import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

    @RestClientTest(JiraServiceImplTest.class)
    @Import({JiraServiceImplTest.TestConfig.class, JiraServiceImpl.class})
    @TestPropertySource(properties = {
            "${gpt.function.jira.create.issue.name}" ,
            "${gpt.function.jira.create.issue.description}" ,
            "${gpt.function.jira.create.issue.attr.description.desc}" ,
            "${gpt.function.jira.create.issue.attr.issuetype.desc}" ,
            "${gpt.function.jira.create.issue.attr.issuetype.epic}",
            "${gpt.function.jira.create.issue.attr.issuetype.story}" ,
            "${gpt.function.jira.create.issue.attr.issuetype.task}",
            "${gpt.function.jira.create.issue.attr.issuetype.bug}" ,
            "${gpt.function.jira.create.issue.attr.duedate.desc}" ,
            "${gpt.function.jira.create.issue.attr.summary.format}" ,
            "${gpt.function.jira.create.issue.attr.summary.desc}"
    })
    class JiraServiceImplTest {
        @Autowired MockRestServiceServer server;
        @Autowired ObjectMapper objectMapper;
        @Autowired JiraServiceImpl service;

        @MockitoBean FunctionFacory functionFactory;
        @MockitoBean CreateJiraIssueFunction createJiraIssueFunction;
        @MockitoBean
        GetAllJiraIssuesFunction getAllJiraIssuesFunction;

        @BeforeEach
        void setup() {
            service.setIssueEndpoint("/issue");
            service.setJiraUrl("https://mtroja98.atlassian.net/rest/api/3");
            service.setJavaProjectName("JAVA");
            service.setSearchEndpoint("/search/jql");
            service.setMaxResults("2");
            service.setFields("summary,description,assignee,status,issuetype");
            service.setRetryAttempts(2);
            service.setWaitSeconds(1);
            service.setCreateIssueEndpoint("/issue");
        }

        @Test
        public void shouldReturnSingleIssue() throws JsonProcessingException {
            String uri = service.getJiraUrl() + service.getIssueEndpoint() + "/1";
            var jiraIssue = buildJiraIssue();

            server.expect(requestTo(uri))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(jiraIssue), MediaType.APPLICATION_JSON));
            var issueFromService = service.getIssue("1");
            server.verify();
            assertThat(issueFromService).isEqualTo(jiraIssue);
        }

        @Test
        public void shouldReturnListOfIssues() throws JsonProcessingException {
            var urlToGet = service.getJiraUrl() + service.getSearchEndpoint() + "?jql=project%3D" + service.getJavaProjectName() + "&fields=" + service.getFields() +"&maxResults=" + service.getMaxResults();
            var jiraListOfIssues = new JiraListOfIssues(buildListOfJiraIssues());
            server.expect(requestTo(urlToGet))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(jiraListOfIssues), MediaType.APPLICATION_JSON));
            var issuesFromService = service.getIssues();
            server.verify();
            assertThat(issuesFromService).isEqualTo(jiraListOfIssues.issues());
        }

        @Test
        public void shouldCreateJavaIssue() throws JsonProcessingException {
            var urlToCreate = service.getJiraUrl() + service.getCreateIssueEndpoint();
            server.expect(requestTo(urlToCreate))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(buildCreateIssueResponse()), MediaType.APPLICATION_JSON));
            var response = service.createJavaIssue(objectMapper.writeValueAsString(buildCreateIssueRequest()));
            server.verify();
            assertThat(response).isEqualTo(buildCreateIssueResponse());
        }

        @Test
        public void shouldThrowInvalidJsonException() {
            var urlToCreate = service.getJiraUrl() + service.getCreateIssueEndpoint();

            for (int i = 0; i < service.getRetryAttempts() - 1; i++) {
                server.expect(requestTo(urlToCreate))
                        .andExpect(method(HttpMethod.POST))
                        .andRespond(withServerError());
            }
            assertThatThrownBy( () -> service.createJavaIssue(getIncorrectJson()))
                            .isInstanceOf(JiraCommunicationException.class);
        }

        @Test
        public void shouldFailWhenResponseCannotBeParsedToJiraCreateIssueResponse() {
            var urlToCreate = service.getJiraUrl() + service.getCreateIssueEndpoint();
            for (int i = 0; i < service.getRetryAttempts(); i++) {
                server.expect(requestTo(urlToCreate))
                        .andExpect(method(HttpMethod.POST))
                        .andRespond(withSuccess("""
                       {
                                    "id": 123,
                                    "key": 456,
                                    "self": {}
                        }
                       """, MediaType.APPLICATION_JSON));
            }
            assertThatThrownBy(() ->
                    service.createJavaIssue(objectMapper.writeValueAsString(buildCreateIssueRequest())))
            .isInstanceOf(JiraCommunicationException.class)
            .hasCauseInstanceOf(JiraCommunicationException.class);

            server.verify();
        }

        private JiraIssue buildJiraIssue() {
            var issueType = new JiraIssue.Issuetype( "Task");
            var contentOfContents = List.of( new JiraIssue.ContentOfContent("text", "test ContentOfContent"));
            var contents = List.of(new JiraIssue.Content(("text"), contentOfContents));
            var description = new JiraIssue.Description("text", 123, contents);
            var project = new JiraIssue.Project("JAVA");
            var status = new JiraIssue.Status("NEW");

            return new JiraIssue("Key", "2025-11-15", "assignee",
                    new JiraIssue.Fields(
                            issueType,
                            description,
                            project,
                            "Test summary", status));
        }

        private List<JiraIssue> buildListOfJiraIssues() {

            var issueType = new JiraIssue.Issuetype( "JAVA");
            var contentOfContents = List.of( new JiraIssue.ContentOfContent("text", "test ContentOfContent"));
            var contents = List.of(new JiraIssue.Content(("text"), contentOfContents));
            var description = new JiraIssue.Description("text", 123, contents);
            var project = new JiraIssue.Project("JAVA");
            var status = new JiraIssue.Status("NEW");

            var jiraIssue1 =  new JiraIssue("Key", "2025-11-15", "assignee",
                    new JiraIssue.Fields(
                            issueType,
                            description,
                            project,
                            "Test summary", status));
            var jiraIssue2 =  new JiraIssue("JAVA-2", "2025-11-16", "assignee1",
                    new JiraIssue.Fields(
                            issueType,
                            description,
                            project,
                            "Test summary1",
                            status));
            return List.of(jiraIssue1, jiraIssue2);
        }

        private JiraCreateIssueRequest buildCreateIssueRequest() {
            var issueType = new JiraCreateIssueRequest.Issuetype( "JAVA");
            var contentOfContents = List.of( new JiraCreateIssueRequest.ContentOfContent("text", "test ContentOfContent req"));
            var contents = List.of(new JiraCreateIssueRequest.Content(("text"), contentOfContents));
            var description = new JiraCreateIssueRequest.Description("text", 123, contents);
            var project = new JiraCreateIssueRequest.Project("JAVA");
            return new JiraCreateIssueRequest("Key", "2025-11-17", "assignee",
                    new JiraCreateIssueRequest.Fields(
                            issueType,
                            description,
                            project,
                            "Test summary2"));
        }


        private JiraCreateIssueResponse buildCreateIssueResponse() {
            return new JiraCreateIssueResponse("10001", "JAVA-1", "https://mtroja98.atlassian.net/rest/api/3/issue/10000");
        }

        private String getIncorrectJson() {
            return """
                    {
                    "test":"test",,
                      "fields": {
                        "project": {
                          "key": "JAVA"
                        },
                        "summary": "REST ye merry gentlemen.",
                        "description": {
                          "type": "doc",
                          "version": 1,
                          "content": [
                            {
                              "type": "paragraph",
                              "content": [
                                {
                                  "type": "text",
                                  "text": "Creating of an issue using project keys and issue type names using the REST API."
                                }
                              ]
                            }
                          ]
                        },
                        "issuetype": {
                          "name": "Task"
                        }
                      }
                    }
                    """;
        }
        private JiraIssueDto buildJiraIssueDto() {
            return new JiraIssueDto(
                    "JAVA-1",
                    "Test summary",
                    "test ContentOfContent",
                    "2025-11-15",
                    "assignee",
                    "Task");
        }

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Qualifier("jiraRestClient")
            RestClient jiraRestClient(RestClient.Builder builder) {
                return builder.baseUrl("https://mtroja98.atlassian.net/rest/api/3").build();
            }

            @Bean("defineCreateJiraIssueFunction")
            public GptFunction defineCreateJiraIssueFunction(
                    @Value("${gpt.function.jira.create.issue.name}") String functionName,
                    @Value("${gpt.function.jira.create.issue.description}") String description,
                    @Value("${gpt.function.jira.create.issue.attr.description.desc}") String descrAttrDescription,
                    @Value("${gpt.function.jira.create.issue.attr.issuetype.desc}") String issueTypeAttrDescription,
                    @Value("${gpt.function.jira.create.issue.attr.issuetype.epic}") String epicIssueType,
                    @Value("${gpt.function.jira.create.issue.attr.issuetype.story}") String storyIssueType,
                    @Value("${gpt.function.jira.create.issue.attr.issuetype.task}") String taskIssueType,
                    @Value("${gpt.function.jira.create.issue.attr.issuetype.bug}") String bugIssueType,
                    @Value("${gpt.function.jira.create.issue.attr.duedate.desc}") String dueDateAttrDescription,
                    @Value("${gpt.function.jira.create.issue.attr.summary.format}") String dueDateFormat,
                    @Value("${gpt.function.jira.create.issue.attr.summary.desc}") String summaryAttrDescription
            )
            {
                var gptFunction = new GptFunction();
                gptFunction.setName(functionName);
                gptFunction.setDescription(description);

                // Predefine function parameters
                JiraCreateIssueParameterProperties properties = new JiraCreateIssueParameterProperties();
                properties.setDescription(new JiraCreateIssueParameterProperties.Description("string",descrAttrDescription));
                properties.setSummary(new JiraCreateIssueParameterProperties.Summary("string",summaryAttrDescription));
                properties.setDuedate(new JiraCreateIssueParameterProperties.DueDate("string",dueDateAttrDescription, dueDateFormat));
                properties.setIssueType(new JiraCreateIssueParameterProperties.IssueType("string",issueTypeAttrDescription, new String[] {epicIssueType, storyIssueType, taskIssueType, bugIssueType} ));

                // Define function's parameters
                GptFunction.Parameters parameters = gptFunction.new Parameters();
                parameters.setType("object");
                parameters.setProperties(properties);
                parameters.setRequired(new String[] {"summary", "description", "issuetype"});

                gptFunction.setParameters(parameters);

                return gptFunction;
            }

            @Bean("defineAllJiraIssuesFunction")
            public GptFunction defineGetAllJiraIssuesFunction() {
                var gptFunction = new GptFunction();
                gptFunction.setName("getAllJiraIssuesFunction");
                gptFunction.setDescription("Get basic data about all issues in my Jira: key, assignee, description, summary, due date.");

                return gptFunction;
            }


        }
    }
