package com.michal.openai.dto;

import java.util.List;

public class GithubRepoDto {
	
	String repositoryName;
	String ownerLogin;
	List<BranchDto> branches;
	public String getRepositoryName() {
		return repositoryName;
	}
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}
	public String getOwnerLogin() {
		return ownerLogin;
	}
	public void setOwnerLogin(String ownerLogin) {
		this.ownerLogin = ownerLogin;
	}
	public List<BranchDto> getBranches() {
		return branches;
	}
	public void setBranches(List<BranchDto> branches) {
		this.branches = branches;
	}
	@Override
	public String toString() {
		return "RepositoryDto [repositoryName=" + repositoryName + ", ownerLogin=" + ownerLogin + ", branches="
				+ branches + "]";
	}
	
	
}
