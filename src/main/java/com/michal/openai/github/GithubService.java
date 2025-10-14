package com.michal.openai.github;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface GithubService {
	CompletableFuture<String> getUserReposWithBranches(String username) throws IOException;


}
