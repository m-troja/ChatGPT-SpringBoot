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
@GptToolAnnotation(name =  "getIssuesBySlackUserIdTool", description ="Returns list of issues by slackUserId" )
public class GetTaskSystemIssuesForUserFunction implements ToolExecutor<GetTaskSystemIssuesForUserFunction.Args> {

    private final TaskSystemService taskSystemService;

    public GetTaskSystemIssuesForUserFunction(TaskSystemService taskSystemService) {
        this.taskSystemService = taskSystemService;
    }

    @Data
    public static class Args {
        @ToolParamAnnotation(description = "Param of user's slackUserId (string in syntax slackUserId extracted from <@slackUserId>)", isRequired = true)
        String slackUserId;

        @ToolParamAnnotation(description = "If issues with specific priority was requested, possible enums: LOW, NORMAL, HIGH, CRITICAL")
        String priority;

        @ToolParamAnnotation(description = "If specific status was requested, possible enums: NEW, TRIAGE, TODO,IN_PROGRESS,WAITING_FOR_TEAM, CODE_REVIEW, DONE, CANCELED")
        String status;
    }


    @Override
    public Object execute(Args arguments) {

        Args args = arguments;
        log.debug("Args: {}", arguments);
        log.debug("args.slackUserId: {}", args.slackUserId);

        return taskSystemService.getIssuesForUser(args.slackUserId);
    }
}