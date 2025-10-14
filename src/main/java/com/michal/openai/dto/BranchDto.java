package com.michal.openai.dto;

public class BranchDto {
	
	String branchName;
	String lastCommitSha;
	
	public BranchDto(String branchName, String lastCommitSha) {
		this.branchName = branchName;
		this.lastCommitSha = lastCommitSha;
	}
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getLastCommitSha() {
		return lastCommitSha;
	}
	public void setLastCommitSha(String lastCommitSha) {
		this.lastCommitSha = lastCommitSha;
	}
	@Override
	public String toString() {
		return "BranchDto [branchName=" + branchName + ", lastCommitSha=" + lastCommitSha + "]";
	}
	public BranchDto() {
		super();
	}
	
	

}
