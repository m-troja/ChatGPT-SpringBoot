package com.michal.openai.gpt.tool.tools;

import com.michal.openai.github.GithubService;
import com.michal.openai.gpt.tool.annotation.GptToolAnnotation;
import com.michal.openai.gpt.tool.annotation.ToolParamAnnotation;
import com.michal.openai.gpt.tool.executor.ToolExecutor;
import com.michal.openai.tasksystem.service.TaskSystemService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@GptToolAnnotation(name =  "githubGetReposTool", description ="Gt data about user's public github repositories, branches, commits" )
public class GithubGetReposByUserTool implements ToolExecutor<GithubGetReposByUserTool.Args> {

    private final GithubService githubService;

    public GithubGetReposByUserTool( GithubService githubService) {
        this.githubService = githubService;
    }

    @Data
    public static class Args
    {
        @ToolParamAnnotation(description = "User's github account login (username). Return string only, no JSON syntax" )
        String login;
    }


    @Override
    public Object execute(Args arguments) {
        Args args = arguments;

        log.debug("githubGetReposTool args.login: {}", args.login);

        try {
            return githubService.getUserReposWithBranches(args.login);
        } catch (IOException e) {
            log.error("Error in githubGetReposTool: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}