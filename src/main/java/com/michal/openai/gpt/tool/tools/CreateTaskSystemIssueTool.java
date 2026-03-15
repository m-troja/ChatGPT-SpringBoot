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
@GptToolAnnotation(name = "createTaskSystemIssueTool",
        description ="The function creates Task-System issue, with the specific Title, description, Priority, AuthorId, AssigneeId, DueDate, ProjectId. Remember that \"returned key\" is the ticket/issue ID created by Task-System! Always return ticket key when answering. Always use slack mention to say who is author and who is assignee of ticket" )
public class CreateTaskSystemIssueTool implements ToolExecutor<CreateTaskSystemIssueTool.Args> {

    private final TaskSystemService taskSystemService;

    public CreateTaskSystemIssueTool(TaskSystemService taskSystemService) {
        this.taskSystemService = taskSystemService;
    }

    @Data
    public static class Args
    {
        @ToolParamAnnotation(description = "Title of the issue" , isRequired = true)
        String title;

        @ToolParamAnnotation(description = "Contains the detailed description of the task", isRequired = true)
        String description;

        @ToolParamAnnotation(description = "Priority String: LOW, NORMAL, HIGH, CRITICAL" )
        String priority;

        @ToolParamAnnotation(description = "Creator of the issue = slackID of user who sent the message to you. Always contains 11 chars and starts with 'U' char", isRequired = true)
        String authorslackid;

        @ToolParamAnnotation(description = "SlackId of user who should be assigned to a ticket. if not defined, then leave empty.")
        String assigneeslackid;

        @ToolParamAnnotation(description = "Contains the due date of the work item, format Example \"2025-10-22\". always is UTC" )
        String duedate;

        @ToolParamAnnotation(description = "Integer value of projectId")
        String projectid;
    }


    @Override
    public Object execute(Args arguments) {

        log.debug("Executing createTaskSystemIssueTool with args: {}", arguments);

        return taskSystemService.createIssue(arguments);
    }
}