package com.michal.openai.github;

import com.michal.openai.entity.GithubRepoDto;

import java.io.IOException;
import java.util.List;

public interface GithubService {
    List<GithubRepoDto> getUserReposWithBranches(String username) throws IOException;


}
