package com.michal.openai.entity;

import lombok.Data;

@Data
public class JiraCreateIssueParameterProperties implements ParameterProperties {
	
	private Description description;
	private Summary summary;
	private DueDate duedate;
	private IssueType issueType;

	public class Description extends ParameterPropertyAttribute 
	{

		public Description() {
			super();
		}

		public Description(String type, String description) {
			super(type, description);
		}
	
	}
	@Data
	public class IssueType extends ParameterPropertyAttribute
	{
		private String[] issueTypes;
		
		public IssueType() {
			super();
		}

		public IssueType(String type, String description, String[] issueTypes) {
			super(type, description);
			this.issueTypes = issueTypes;
		}
	}
	
	public class Summary extends ParameterPropertyAttribute
	{

		public Summary() {
			super();
		}

		public Summary(String type, String description) {
			super(type, description);
		}
		
	}
	
	public class DueDate extends ParameterPropertyAttribute
	{

        private String format;
		
		public DueDate() {
			super();
		}

		public DueDate(String type, String description, String format) {
			super(type, description);
			this.format = format;
		}
	}
	
}
