package com.michal.openai.github.impl;

import com.michal.openai.github.entity.GithubBranch;
import com.michal.openai.github.entity.GithubCommit;
import com.michal.openai.github.entity.GithubRepoDto;
import com.michal.openai.github.entity.GithubRepoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

@Import(GithubServiceImplTest.TestConfig.class)
@RestClientTest(GithubServiceImpl.class)
public class GithubServiceImplTest {

    @Autowired MockRestServiceServer server;
    @Autowired GithubServiceImpl service;

    @Test
    public void shouldReturnTwoUserRepos() {
        var githubResponse1 = new GithubRepoResponse(1L, "Repo1", "FullName1", "Desc1", "Html1", "CloneUrl1", "SshUrl1", "Language1", 1, 1, 1, 1, false, new GithubRepoResponse.Owner(1L, "login1", "htmlUrl1", "avatarUrl1"));
        var githubResponse2 = new GithubRepoResponse(1L, "Repo1", "FullName1", "Desc1", "Html1", "CloneUrl1", "SshUrl1", "Language1", 1, 1, 1, 1, true, new GithubRepoResponse.Owner(1L, "login1", "htmlUrl1", "avatarUrl1"));
        List<GithubRepoResponse> repoResponses = List.of(githubResponse1, githubResponse2);

        var githubBranch1 = new GithubBranch("main1", new GithubCommit("commit1", "url1"));
        var githubBranch2 = new GithubBranch("main2", new GithubCommit("commit2", "url2"));
        var githubBranch3 = new GithubBranch("main3", new GithubCommit("commit3", "url3"));
        List<GithubBranch> branchesRepo1 = List.of(githubBranch1, githubBranch2);
        List<GithubBranch> branchesRepo2 = List.of(githubBranch3);

        List<GithubRepoDto> notForkedDtos = List.of(
                new GithubRepoDto(githubResponse1.name(), githubResponse1.description(), List.of(githubBranch1, githubBranch2), githubResponse1.fork())
        );

        List<GithubRepoDto> forkedDtos = List.of(
                new GithubRepoDto(githubResponse2.name(), githubResponse2.description(), List.of(githubBranch3), githubResponse2.fork())
        );


    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Qualifier("taskSystemRestClient")
        RestClient taskSystemRestClient(RestClient.Builder builder) {
            String baseUrl = "https://api.github.com";
            return builder.baseUrl(baseUrl).build();
        }
    }
}