package com.michal.openai.github.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.github.entity.GithubBranch;
import com.michal.openai.github.entity.GithubCommit;
import com.michal.openai.github.entity.GithubRepoDto;
import com.michal.openai.github.entity.GithubRepoResponse;
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
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@Import(GithubServiceImplTest.TestConfig.class)
@RestClientTest(GithubServiceImpl.class)
public class GithubServiceImplTest {

    @Autowired MockRestServiceServer server;
    @Autowired GithubServiceImpl service;
    @Autowired ObjectMapper objectMapper;
    private String baseUrl;

    @BeforeEach
    public void init() {
        baseUrl = "https://api.github.com";
    }

    @Test
    public void shouldReturnTwoUserRepos() throws JsonProcessingException {

        var owner = new GithubRepoResponse.Owner(1L, "login1", "htmlUrl1", "avatarUrl1");
        String uri = "/users/" + owner.login() + "/repos";
        var githubResponseNotForked = new GithubRepoResponse(1L, "Repo1", "FullName1", "Desc1", "Html1", "CloneUrl1", "SshUrl1", "Language1", 1, 1, 1, 1, false, owner);
        var githubResponseForked = new GithubRepoResponse(1L, "Repo1", "FullName1", "Desc1", "Html1", "CloneUrl1", "SshUrl1", "Language1", 1, 1, 1, 1, true,  owner);
        var userRepos = List.of(githubResponseNotForked, githubResponseForked);
        var notForkedRepos = List.of(githubResponseNotForked);

        var githubBranch1 = new GithubBranch("main1", new GithubCommit("commit1", "url1"));
        var githubBranch2 = new GithubBranch("main2", new GithubCommit("commit2", "url2"));
        var notForkedBranches = List.of(githubBranch1, githubBranch2);

        List<GithubRepoDto> notForkedDtos = List.of(
                new GithubRepoDto(githubResponseNotForked.name(), githubResponseNotForked.owner().login(), notForkedBranches, githubResponseNotForked.fork())
        );

        //  fetchUserRepos
        String urlFetchRepos = baseUrl + uri;
        server.expect(requestTo(urlFetchRepos))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(userRepos), MediaType.APPLICATION_JSON));

        // fetchBranches
        notForkedRepos.forEach( repo -> {
            String uriFetchBranches = baseUrl + "/repos/" + repo.owner().login() + "/" + repo.name() + "/branches";
            try {
                server.expect(requestTo(uriFetchBranches))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withSuccess( objectMapper.writeValueAsString(notForkedBranches), MediaType.APPLICATION_JSON));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        List<GithubRepoDto> dtosFromService = service.getUserReposWithBranches(owner.login());
        server.verify();
        assertThat(dtosFromService).hasSize(1);
        assertThat(dtosFromService).isEqualTo(notForkedDtos);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Qualifier("githubRestClient")
        RestClient githubRestClient(RestClient.Builder builder) {
            String baseUrl = "https://api.github.com";
            return builder.baseUrl(baseUrl).build();
        }
    }
}