package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // deserialize only known fields
public record GithubRepoDto(String name , String owner, List<GithubBranch> branches, @JsonIgnore boolean isFork) {}
