package com.michal.openai.tasksystem.entity;

import com.michal.openai.functions.entity.ParameterProperties;
import com.michal.openai.functions.entity.ParameterPropertyAttribute;
import lombok.Data;

@Data
public class TaskSystemAssignIssueParameterProperties implements ParameterProperties  {

    private TaskSystemAssignIssueParameterProperties.Key key;
    private TaskSystemAssignIssueParameterProperties.SlackUserId slackUserId;

    @Data
    public class Key extends ParameterPropertyAttribute
    {
        public Key() {
            super();
        }
        public Key(String type, String description) {
            super(type, description);
        }

    }@Data
    public class SlackUserId extends ParameterPropertyAttribute
    {
        public SlackUserId() {
            super();
        }
        public SlackUserId(String type, String description) {
            super(type, description);
        }

    }


}
