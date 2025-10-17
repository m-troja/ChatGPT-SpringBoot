package com.michal.openai.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // deserialize only known fields
public record GithubRepo(String name , GithubOwner owner, List<GithubBranch> branches, @JsonIgnore boolean isFork) {}
