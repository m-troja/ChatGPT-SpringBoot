package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubCommit(String sha, @JsonIgnoreProperties String url) {}

