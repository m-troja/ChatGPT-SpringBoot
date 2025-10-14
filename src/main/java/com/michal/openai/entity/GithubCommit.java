package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommit {
	
	String sha;
	
	@JsonIgnoreProperties
	String url;
	
	public GithubCommit(String sha) {
		this.sha = sha;
	}

	public GithubCommit() {
		super();
	}

	@Override
	public String toString() {
		return "GithubCommit [sha=" + sha + ", url=" + url + "]";
	}
	
	
}

