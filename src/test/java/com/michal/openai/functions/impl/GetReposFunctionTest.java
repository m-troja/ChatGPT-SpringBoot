package com.michal.openai.functions.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.github.GithubService;
import com.michal.openai.github.entity.GithubBranch;
import com.michal.openai.github.entity.GithubCommit;
import com.michal.openai.github.entity.GithubRepoDto;
import com.michal.openai.github.entity.GithubRepoResponse;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetReposFunctionTest {

    private GithubService githubService;
    private ObjectMapper objectMapper;
    private GetReposFunction function;

    @BeforeEach
    public void init() {
        githubService = mock(GithubService.class);
        objectMapper = new ObjectMapper();
        function = new GetReposFunction(githubService, objectMapper);
    }

    public void shouldReturnUserRepos() throws IOException {
        // given
        var repoWithBranchesMock = buildGithubRepoDtos();
        when(githubService.getUserReposWithBranches("login")).thenReturn(buildGithubRepoDtos());

        // when
        String functionResponse = function.execute(objectMapper.writeValueAsString("login"));

        // then
        var repoWithBranchesParsedFromService = objectMapper.readValue(functionResponse, new TypeReference<List<GithubRepoDto>>() {  });
        assertThat(repoWithBranchesMock).isEqualTo(repoWithBranchesParsedFromService);
    }

    private List<GithubRepoDto> buildGithubRepoDtos() {
        var owner = new GithubRepoResponse.Owner(1L, "login", "htmlUrl1", "avatarUrl1");
        var repoResponse = new GithubRepoResponse(1L, "Repo1", "FullName1", "Desc1", "Html1", "CloneUrl1", "SshUrl1", "Language1", 1, 1, 1, 1, false, owner);
        var userReposList = List.of(repoResponse);
        var branch1 = new GithubBranch("main1", new GithubCommit("commit1", "url1"));
        var branch2 = new GithubBranch("main2", new GithubCommit("commit2", "url2"));
        var branchesList = List.of(branch1, branch2);
        var repoWithBranchesList = List.of(new GithubRepoDto(repoResponse.name(), repoResponse.owner().login(), branchesList, repoResponse.fork()));
        return repoWithBranchesList;
    }
}
