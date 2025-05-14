package com.michal.openai.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JiraCreateIssueRequest {
	
	String returnedKey;
	
	public String getReturnedKey() {
		return returnedKey;
	}


	public void setReturnedKey(String returnedKey) {
		this.returnedKey = returnedKey;
	}


	private Fields fields;

	public Fields getFields() {
		return fields;
	}


	public void setFields(Fields fields) {
		this.fields = fields;
	}


	@Override
	public String toString() {
		return "JiraCreateIssueRequest [fields=" + fields + "]";
	}


	public class Fields
	{
		String summary;
		
        @JsonProperty("issuetype")  // Tutaj wymuszamy "issuetype" w JSON
		 private Issuetype issueType;
		 private Description description;
		 private Project project;
		 
		
		public Project getProject() {
			return project;
		}


		public void setProject(Project project) {
			this.project = project;
		}


		public Issuetype getIssueType() {
			return issueType;
		}


		public void setIssueType(Issuetype issueType) {
			this.issueType = issueType;
		}




		public Description getDescription() {
			return description;
		}


		public void setDescription(Description description) {
			this.description = description;
		}


		public Fields(String summary) {
			this.summary = summary;
		}


		public String getSummary() {
			return summary;
		}


		public void setSummary(String summary) {
			this.summary = summary;
		}


	

		@Override
		public String toString() {
			return "Fields [summary=" + summary + ", issueType=" + issueType + ", description=" + description + "]";
		}



		public class Project
		{
			String key;

			public String getKey() {
				return key;
			}

			public void setKey(String key) {
				this.key = key;
			}

			@Override
			public String toString() {
				return "Project [key=" + key + "]";
			}

			public Project() {
			}

			public Project(String key) {
				super();
				this.key = key;
			}

	
			
		}	//END project
		public class Issuetype
		{
			String name;

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public Issuetype(String name) {
				this.name = name;
			}

			@Override
			public String toString() {
				return "Issuetype [name=" + name + "]";
			}
			
		}

		public class Description
		{
			String type;
			Integer version;
			private List<Content> content;
			
			
			public List<Content> getContent() {
				return content;
			}

			public void setContent(List<Content> content) {
				this.content = content;
			}

			public Description(String type, Integer version) {
				this.type = type;
				this.version = version;
			}

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}

			public Integer getVersion() {
				return version;
			}

			public void setVersion(Integer version) {
				this.version = version;
			}
			
			@Override
			public String toString() {
				return "Description [type=" + type + ", version=" + version + "]";
			}

			public Description() {
				super();
			}
			
			public class Content
			{
				String type;

				@JsonProperty("content")
				private List<ContentOfContent> contentOfContent;
				
				public List<ContentOfContent> getContentOfContent() {
					return contentOfContent;
				}

				public void setContentOfContent(List<ContentOfContent> contentOfContent) {
					this.contentOfContent = contentOfContent;
				}

				public Content(String type, String text) {
					this.type = type;
				}

				public String getType() {
					return type;
				}

				public void setType(String type) {
					this.type = type;
				}


				public Content() {
					super();
				}
				
				public class ContentOfContent
				{
					String type;
					String text;
					public ContentOfContent() {
						super();
					}
					public String getType() {
						return type;
					}
					public void setType(String type) {
						this.type = type;
					}
					public String getText() {
						return text;
					}
					public void setText(String text) {
						this.text = text;
					}
					
					
				}
				
			} //END CONTENT 1

			
		} // END description

	}
}
