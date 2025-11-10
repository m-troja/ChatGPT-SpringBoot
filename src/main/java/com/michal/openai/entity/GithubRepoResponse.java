package com.michal.openai.entity;

public record GithubRepoResponse(
        long id,
        String name,
        String fullName,
        String description,
        String htmlUrl,
        String cloneUrl,
        String sshUrl,
        String language,
        int forksCount,
        int stargazersCount,
        int watchersCount,
        int openIssuesCount,
        boolean fork,
        Owner owner
) {
    public record Owner(
            long id,
            String login,
            String htmlUrl,
            String avatarUrl
    ) {}
}