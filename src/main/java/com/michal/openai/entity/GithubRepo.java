package com.michal.openai.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // deserialize only known fields
public class GithubRepo {

	private String name;
	private GithubOwner owner;
	private List<GithubBranch> branches;
	
	@JsonIgnore
	private boolean fork;



	public List<GithubBranch> getBranches() {
		return branches;
	}

	public void setBranches(List<GithubBranch> branches) {
		this.branches = branches;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GithubOwner getOwner() {
		return owner;
	}

	public void setOwner(GithubOwner owner) {
		this.owner = owner;
	}

	public boolean isFork() {
		return fork;
	}

	public void setFork(boolean fork) {
		this.fork = fork;
	}



	@Override
	public String toString() {
		return "GithubRepo [name=" + name + ", owner=" + owner + ", branches=" + branches + ", fork=" + fork + "]";
	}

	public GithubRepo() {
	}

	
}
