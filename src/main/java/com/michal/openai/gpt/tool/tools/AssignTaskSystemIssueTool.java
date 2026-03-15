package com.michal.openai.gpt.tool.tools;

import com.michal.openai.gpt.tool.annotation.GptToolAnnotation;
import com.michal.openai.gpt.tool.annotation.ToolParamAnnotation;
import com.michal.openai.gpt.tool.executor.ToolExecutor;
import com.michal.openai.tasksystem.service.TaskSystemService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@GptToolAnnotation(name = "assignTaskSystemIssueTool", description ="assign issue to slackUserId by issue-key" )
public class AssignTaskSystemIssueTool implements ToolExecutor<AssignTaskSystemIssueTool.Args> {

    private final TaskSystemService taskSystemService;

    public AssignTaskSystemIssueTool(TaskSystemService taskSystemService) {
        this.taskSystemService = taskSystemService;
    }

    @Data
    public static class Args
    {
        @ToolParamAnnotation(description = "key of the issue, format i.e. \"Dummy-1\"" )
        String key;

        @ToolParamAnnotation(description = "slackUserId to which ticket should be assigned, i.e. \"U1234AB\"" )
        String slackUserId;
    }


    @Override
    public Object execute(Args arguments) {

        Args args = arguments;

        log.debug("Executing assignTaskSystemIssueTool with args: {}", args);
        try {
            return taskSystemService.assignIssue(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}