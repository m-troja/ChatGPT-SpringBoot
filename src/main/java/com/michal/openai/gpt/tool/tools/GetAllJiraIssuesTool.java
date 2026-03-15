package com.michal.openai.gpt.tool.tools;

import com.michal.openai.gpt.tool.annotation.GptToolAnnotation;
import com.michal.openai.gpt.tool.executor.ToolExecutor;
import com.michal.openai.jira.service.JiraService;
import com.michal.openai.tasksystem.service.TaskSystemService;
import org.springframework.stereotype.Component;

@Component
@GptToolAnnotation(name =  "getAllJiraIssuesTool", description ="Get basic data about all issues in my Jira: key, assignee, description, summary, due date etc" )
public class GetAllJiraIssuesTool implements ToolExecutor<GetAllJiraIssuesTool.Args> {

    private final JiraService jiraService;

    public GetAllJiraIssuesTool( JiraService jiraService) {
        this.jiraService = jiraService;
    }

    public static class Args
    {
    }


    @Override
    public Object execute(Args arguments) {

        return jiraService.getIssues();
    }
}