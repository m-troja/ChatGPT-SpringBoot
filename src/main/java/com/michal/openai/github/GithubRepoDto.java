package com.michal.openai.github;

import com.michal.openai.github.entity.BranchDto;

import java.util.List;

public record GithubRepoDto(String repositoryName, String ownerLogin, List<BranchDto> branches) {}
