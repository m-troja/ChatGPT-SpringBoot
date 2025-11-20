    package com.michal.openai.jira.impl;

    import com.fasterxml.jackson.core.JsonProcessingException;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.michal.openai.config.BeansConfiguration;
    import com.michal.openai.exception.JiraCommunicationException;
    import com.michal.openai.jira.entity.JiraCreateIssueRequest;
    import com.michal.openai.jira.entity.JiraCreateIssueResponse;
    import com.michal.openai.jira.entity.JiraIssue;
    import com.michal.openai.jira.entity.JiraListOfIssues;
    import com.michal.openai.jira.service.impl.JiraServiceImpl;
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
    import org.springframework.test.context.TestPropertySource;
    import org.springframework.test.web.client.MockRestServiceServer;
    import org.springframework.web.client.RestClient;

    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.assertj.core.api.Assertions.assertThatThrownBy;
    import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
    import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
    import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

    @RestClientTest(JiraServiceImplTest.class)
    @Import({JiraServiceImplTest.TestConfig.class, JiraServiceImpl.class, BeansConfiguration.class})
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
            var response = service.createJavaIssue(buildCreateIssueRequest());
            server.verify();
            assertThat(response).isEqualTo(buildCreateIssueResponse());
        }

//        @Test
//        public void shouldThrowInvalidJsonException() {
//            var urlToCreate = service.getJiraUrl() + service.getCreateIssueEndpoint();
//
//            for (int i = 0; i < service.getRetryAttempts() - 1; i++) {
//                server.expect(requestTo(urlToCreate))
//                        .andExpect(method(HttpMethod.POST))
//                        .andRespond(withServerError());
//            }
//            assertThatThrownBy( () -> service.createJavaIssue(getIncorrectJson()))
//                            .isInstanceOf(JiraCommunicationException.class);
//        }

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
                    service.createJavaIssue(buildCreateIssueRequest()))
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
            var issueType = new JiraCreateIssueRequest.Issuetype( "Task");
            var contentOfContents = List.of( new JiraCreateIssueRequest.ContentOfContent("text", "Test desc"));
            var contents = List.of(new JiraCreateIssueRequest.Content(("paragraph"), contentOfContents));
            var description = new JiraCreateIssueRequest.Description("doc", 1, contents);
            var project = new JiraCreateIssueRequest.Project("JAVA");
            return new JiraCreateIssueRequest(
                    new JiraCreateIssueRequest.Fields(
                            issueType,
                            description,
                            project,
                            "Test summary2"));
        }


        private JiraCreateIssueResponse buildCreateIssueResponse() {
            return new JiraCreateIssueResponse("10001", "JAVA-1", "https://mtroja98.atlassian.net/rest/api/3/issue/10000");
        }

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Qualifier("jiraRestClient")
            RestClient jiraRestClient(RestClient.Builder builder) {
                return builder.baseUrl("https://mtroja98.atlassian.net/rest/api/3").build();
            }
        }
    }
