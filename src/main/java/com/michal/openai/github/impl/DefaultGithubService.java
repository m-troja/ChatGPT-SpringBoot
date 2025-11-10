package com.michal.openai.github.impl;

import com.michal.openai.github.entity.GithubBranch;
import com.michal.openai.github.entity.GithubRepoDto;
import com.michal.openai.github.entity.GithubRepoResponse;
import com.michal.openai.exception.UserNotFoundException;
import com.michal.openai.github.GithubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultGithubService implements GithubService {

    private final RestClient githubRestClient;

    @Override
    public List<GithubRepoDto> getUserReposWithBranches(String username) {

        List<GithubRepoResponse> results = fetchUserRepos(username);
        List<GithubRepoResponse> notForked = extractNotForkedRepos(results);
        return assignBranchesToRepos(notForked);
    }

    private List<GithubRepoResponse> fetchUserRepos(String username) {
        String uri = "/users/" + username + "/repos";

        log.debug("Fetching repos from: {}, user: {}", uri, username);

        try {
            List<GithubRepoResponse> githubRepoResponse = githubRestClient.get()
        			.uri(uri)
	        		.retrieve()
	        		.body(new ParameterizedTypeReference<>() {});
            log.debug("Github response: {}", githubRepoResponse);

            return githubRepoResponse;

        }
        catch(HttpClientErrorException e)
        {
        	if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        		throw new UserNotFoundException(username);
        	}
        	throw new RuntimeException("Github API error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        }
        catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Unexpected error calling GitHub API", e);
        }
    }

    private List<GithubRepoResponse> extractNotForkedRepos(List<GithubRepoResponse> githubRepoResponses) {
        List<GithubRepoResponse> repos = githubRepoResponses.stream().filter( r -> !r.fork()).toList();
        return repos.stream()
                .filter(repo -> !repo.fork())
                .collect(Collectors.toList());
    }

    private List<GithubRepoDto> assignBranchesToRepos(List<GithubRepoResponse> notForkedRepos) {
        List<GithubRepoDto> updatedRepos = new ArrayList<>();
        for (GithubRepoResponse repo : notForkedRepos) {
            var branches = fetchBranches(repo);
            updatedRepos.add(new GithubRepoDto(repo.name(), repo.owner().login(), branches, repo.fork()));
        }
        return updatedRepos;
    }

    private List<GithubBranch> fetchBranches(GithubRepoResponse repo) {
        String uri = "/repos/" + repo.owner().login() + "/" + repo.name() + "/branches";
        log.debug("Calling fetchBranches from uri : {}", uri);

        List<GithubBranch> branches = githubRestClient.get()
    			.uri(uri)
        		.retrieve()
        		.body(new ParameterizedTypeReference<>() {});

        log.debug("fetchBranches results: {}", branches);

        return branches;
    }

	public DefaultGithubService(@Qualifier("githubRestClient") RestClient githubRestClient) {
        this.githubRestClient = githubRestClient;
	}
}
