package com.michal.openai.gpt.tool.tools;

import com.michal.openai.gpt.tool.annotation.GptToolAnnotation;
import com.michal.openai.gpt.tool.annotation.ToolParamAnnotation;
import com.michal.openai.gpt.tool.executor.ToolExecutor;
import com.michal.openai.jira.service.JiraService;
import org.springframework.stereotype.Component;

@Component
@GptToolAnnotation(name = "createJiraIssueTool",
        description ="creates Jira issue" )
public class CreateJiraIssueTool implements ToolExecutor<CreateJiraIssueTool.Args> {

    private final JiraService jiraService;

    public CreateJiraIssueTool(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    public static class Args
    {
        @ToolParamAnnotation(description = "id" )
        String summary;

        @ToolParamAnnotation(description = "detailed description of the task")
        String description;

        @ToolParamAnnotation(description = "Priority String: LOW, NORMAL, HIGH, CRITICAL" )
        String priority;

        @ToolParamAnnotation(description = "name of the team member that should be assigned to the current work item.")
        String assignee;

        @ToolParamAnnotation(description = "Contains the due date of the work item, format Example \"2025-10-22\". always is UTC" )
        String duedate;

        @ToolParamAnnotation(description = "shortname of project. default=JAVA")
        String project;

        @ToolParamAnnotation(description = "type of issue: choose from Epic, Story, Task, Bug")
        String issueType;
    }


    @Override
    public Object execute(Args arguments) {

        Args args = arguments;

        return jiraService.createJavaIssue(arguments);
    }
}