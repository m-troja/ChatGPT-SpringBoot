package com.michal.openai.entity;

import java.util.List;

public class GithubResponse {
	
	List<GithubRepo> githubRepos;
	GithubOwner githubUser;
	
	
	public GithubResponse() {}


	public List<GithubRepo> getGithubRepos() {
		return githubRepos;
	}


	public void setGithubRepos(List<GithubRepo> githubRepos) {
		this.githubRepos = githubRepos;
	}


	public GithubOwner getGithubUser() {
		return githubUser;
	}


	public void setGithubUser(GithubOwner githubUser) {
		this.githubUser = githubUser;
	}

}
