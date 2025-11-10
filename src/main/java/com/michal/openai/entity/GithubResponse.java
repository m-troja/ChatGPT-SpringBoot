package com.michal.openai.entity;

import java.util.List;

public record GithubResponse(List<GithubRepoDto> githubRepoDtos, GithubOwner githubUser ) {}
