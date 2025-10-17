package com.michal.openai.github.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.exception.UserNotFoundException;
import com.michal.openai.dto.GithubRepoDto;
import com.michal.openai.dto.cnv.RepoCnv;
import com.michal.openai.entity.GithubBranch;
import com.michal.openai.entity.GithubRepo;
import com.michal.openai.github.GithubService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DefaultGithubService implements GithubService {

    private final RepoCnv repoCnv;
    private final RestClient githubRestClient;
    private final ObjectMapper objectMapper;
    private record ReposResult(HttpStatus status, String responseBody) {}

    @Override
    public CompletableFuture<String> getUserReposWithBranches(String username) throws IOException {
       
    	ReposResult result = fetchUserRepos(username);
        
        List<GithubRepo> notForked = extractNotForkedRepos(result.responseBody());
        List<GithubRepo> reposWithBranchesAssigned = assignBranchesToRepos(notForked);
        List<GithubRepoDto> repoDtos = repoCnv.convertReposToRepoDtos(reposWithBranchesAssigned);

        return CompletableFuture.completedFuture(objectMapper.writeValueAsString(repoDtos));
    }

    private ReposResult fetchUserRepos(String username) throws IOException {
        String uri = "/users/" + username + "/repos";

        log.debug("fetching repos from url : {}", uri);

        try {
            String body = githubRestClient.get()
        			.uri(uri)
	        		.retrieve()
	        		.body(String.class);
            log.debug("github response: {}", body);

            return new ReposResult(HttpStatus.OK, body);

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

    private List<GithubRepo> extractNotForkedRepos(String responseBody) throws JsonProcessingException {
        List<GithubRepo> repos = objectMapper.readValue(responseBody, new TypeReference<>() {});
        return repos.stream()
                .filter(repo -> !repo.isFork())
                .collect(Collectors.toList());
    }

    private List<GithubRepo> assignBranchesToRepos(List<GithubRepo> repos) throws IOException {
        List<GithubRepo> updatedRepos = new ArrayList<>();
        for (GithubRepo repo : repos) {
            var branches = fetchBranches(repo);
            updatedRepos.add(new GithubRepo(repo.name(), repo.owner(), branches, repo.isFork()));
        }
        return repos;
    }

    private List<GithubBranch> fetchBranches(GithubRepo repo) throws IOException {
        String uri = "/repos/" + repo.owner().login() + "/" + repo.name() + "/branches";
        log.debug("fetchBranches from uri : {}", uri);

        List<GithubBranch> branches = githubRestClient.get()
    			.uri(uri)
        		.retrieve()
        		.body(new ParameterizedTypeReference<List<GithubBranch>>() {});

        log.debug("fetchBranches: {}", branches.toString());

        return branches;
    }

	public DefaultGithubService(RepoCnv repoCnv, @Qualifier("githubRestClient") RestClient githubRestClient, ObjectMapper objectMapper) {
		this.repoCnv = repoCnv;
		this.githubRestClient = githubRestClient;
		this.objectMapper = objectMapper;
	}

}
