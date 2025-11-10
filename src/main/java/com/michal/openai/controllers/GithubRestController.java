package com.michal.openai.controllers;

import com.michal.openai.github.entity.GithubRepoDto;
import com.michal.openai.github.GithubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/api/v1/github")
@RestController
@RequiredArgsConstructor
public class GithubRestController {

    private final GithubService githubService;

    @GetMapping("/user/{login}")
    public List<GithubRepoDto> getUserRepos(@PathVariable String login) throws IOException {
        log.debug("Triggered /api/v1/github/user/{}", login);
        return githubService.getUserReposWithBranches(login);
    }
}
