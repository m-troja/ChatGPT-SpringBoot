package com.michal.openai.entity;

import java.util.List;

public record GithubResponse(List<GithubRepo> githubRepos, GithubOwner githubUser ) {}
