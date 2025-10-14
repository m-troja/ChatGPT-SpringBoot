package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubOwner {
	
	String login;
	
	@JsonIgnore
	String id;
	
	public GithubOwner() {
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	@Override
	public String toString() {
		return "GithubOwner [login=" + login + "]";
	}

}
