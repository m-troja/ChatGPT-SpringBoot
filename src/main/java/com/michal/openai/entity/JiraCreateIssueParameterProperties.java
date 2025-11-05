package com.michal.openai.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class JiraCreateIssueParameterProperties implements ParameterProperties {
	
	private Description description;
	private Summary summary;
	private DueDate duedate;
	private IssueType issueType;

	public class Description extends ParameterPropertyAttribute 
	{
	}
    @EqualsAndHashCode(callSuper = true)
    @Data
	public class IssueType extends ParameterPropertyAttribute
	{
		private String[] issueTypes;

	}
    @EqualsAndHashCode(callSuper = true)
    @Data
    public class Summary extends ParameterPropertyAttribute
	{
	}
    @EqualsAndHashCode(callSuper = true)
    @Data
    public class DueDate extends ParameterPropertyAttribute
	{
        private String format;

	}
	
}
