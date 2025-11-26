package com.michal.openai.tasksystem.entity.function;

import com.michal.openai.functions.entity.ParameterProperties;
import com.michal.openai.functions.entity.ParameterPropertyAttribute;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class TaskSystemCreateIssueParameterProperties implements ParameterProperties  {

    private TaskSystemCreateIssueParameterProperties.Title title;
    private TaskSystemCreateIssueParameterProperties.Description description;
    private TaskSystemCreateIssueParameterProperties.Priority priority;
    private AuthorSlackId authorSlackId;
    private AssigneeSlackId assigneeSlackId;
    private TaskSystemCreateIssueParameterProperties.DueDate dueDate;
    private TaskSystemCreateIssueParameterProperties.ProjectId projectId;

    @EqualsAndHashCode(callSuper = true)
    @Data
    public class Title extends ParameterPropertyAttribute
    {
        public Title() {
            super();
        }
        public Title(String type, String description) {
            super(type, description);
        }

    }
    @EqualsAndHashCode(callSuper = true)
    @Data
    public class Description extends ParameterPropertyAttribute
    {
        public Description() {
            super();
        }
        public Description(String type, String description) {
            super(type, description);
        }

    }
    @EqualsAndHashCode(callSuper = true)
    @Data
    public class Priority extends ParameterPropertyAttribute
    {
        private String[] priorities;
        public Priority() {
            super();
        }
        public Priority(String type, String description, String[] priorities) {
            super(type, description);
            this.priorities = priorities;  }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public class AuthorSlackId extends ParameterPropertyAttribute
    {
        public AuthorSlackId() {
            super();
        }
        public AuthorSlackId(String type, String description) {
            super(type, description);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public class AssigneeSlackId extends ParameterPropertyAttribute
    {
        public AssigneeSlackId() {
            super();
        }
        public AssigneeSlackId(String type, String description) {
            super(type, description); }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public class DueDate extends ParameterPropertyAttribute
    {
        String format;
        public DueDate() {
            super();
        }
        public DueDate(String type, String description, String format) {
            super(type, description);
            this.format = format;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public class ProjectId extends ParameterPropertyAttribute
    {
        public ProjectId() {
            super();
        }
        public ProjectId(String type, String description) {
            super(type, description);
        }

    }

}
