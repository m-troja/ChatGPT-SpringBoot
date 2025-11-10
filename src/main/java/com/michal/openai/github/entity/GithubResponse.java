package com.michal.openai.github.entity;

import java.util.List;

public record GithubResponse(List<GithubRepoDto> githubRepoDtos, GithubOwner githubUser ) {}
