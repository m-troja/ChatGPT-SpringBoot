package com.michal.openai.github;

import java.io.IOException;

public interface GithubService {
	String getUserReposWithBranches(String username) throws IOException;


}
