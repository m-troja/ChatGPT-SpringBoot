package com.michal.openai.jira.entity;

import com.michal.openai.functions.entity.ParameterProperties;
import com.michal.openai.functions.entity.ParameterPropertyAttribute;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class JiraCreateIssueParameterProperties implements ParameterProperties {
	
	private Description description;
	private Summary summary;
	private DueDate duedate;
	private IssueType issueType;

	@Data
    @EqualsAndHashCode(callSuper = true)
    public static class Description extends ParameterPropertyAttribute
	{
        private final String type;
        private final String description;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
	public static class IssueType extends ParameterPropertyAttribute
	{
        private final String type;
        private final String description;
		private final String[] issueTypes;
	}

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Summary extends ParameterPropertyAttribute
	{
        private final String type;
        private final String description;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DueDate extends ParameterPropertyAttribute
	{
        private final String type;
        private final String description;
        private final String format;

	}
	
}
