package com.michal.openai.dto;

import java.util.List;

public record GithubRepoDto(String repositoryName, String ownerLogin, List<BranchDto> branches) {}
