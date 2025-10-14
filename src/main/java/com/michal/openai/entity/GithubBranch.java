package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubBranch {

	private String name;
 	private GithubCommit commit;

	public GithubBranch() {
	}

	public GithubBranch(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public GithubCommit getCommit() {
		return commit;
	}

	public void setCommit(GithubCommit commit) {
		this.commit = commit;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "GithubBranch [name=" + name + ", commit=" + commit + "]";
	}


	

}
