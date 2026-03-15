package com.michal.openai.gpt.tool.tools;

import com.michal.openai.gpt.tool.annotation.GptToolAnnotation;
import com.michal.openai.gpt.tool.executor.ToolExecutor;
import com.michal.openai.tasksystem.service.TaskSystemService;
import org.springframework.stereotype.Component;

@Component
@GptToolAnnotation(name =  "getAllTaskSystemIssuesTool", description ="Get all issues in Task-System: key, assignee, description, summary, due date etc" )
public class GetAllTaskSystemIssuesTool implements ToolExecutor<GetAllTaskSystemIssuesTool.Args> {

    private final TaskSystemService taskSystemService;

    public GetAllTaskSystemIssuesTool(TaskSystemService taskSystemService) {
        this.taskSystemService = taskSystemService;
    }

    public static class Args
    {
    }


    @Override
    public Object execute(Args arguments) {

        return taskSystemService.getAllIssues();
    }
}