package com.michal.openai.entity;

public class JiraCreateIssueParameterProperties implements ParameterProperties {
	
	private Description description;
	private Summary summary;
	private DueDate duedate;
	private IssueType issueType;
	
	public Summary getSummary() {
		return summary;
	}

	public void setSummary(Summary summary) {
		this.summary = summary;
	}

	public DueDate getDuedate() {
		return duedate;
	}

	public void setDuedate(DueDate duedate) {
		this.duedate = duedate;
	}

	public IssueType getIssueType() {
		return issueType;
	}

	public void setIssueType(IssueType issueType) {
		this.issueType = issueType;
	}

	public Description getDescription() {
		return description;
	}

	public void setDescription(Description description) {
		this.description = description;
	}

	public class Description extends ParameterPropertyAttribute 
	{

		public Description() {
			super();
		}

		public Description(String type, String description) {
			super(type, description);
		}
	
	}
	
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

		String[] getIssueTypes() {
			return issueTypes;
		}

		public void setIssueTypes(String[] issueTypes) {
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
		
		String format;
		
		public DueDate() {
			super();
		}

		public DueDate(String type, String description, String format) {
			super(type, description);
			this.format = format;
		}
	}
	
}
