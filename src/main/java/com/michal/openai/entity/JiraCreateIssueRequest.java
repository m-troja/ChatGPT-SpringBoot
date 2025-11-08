package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class JiraCreateIssueRequest {

    private String returnedKey;
	private Fields fields;

	@Override
	public String toString() {
		return "JiraCreateIssueRequest [fields=" + fields + "]";
	}

    @Data
	public class Fields
	{
        @JsonProperty("issuetype")  // Tutaj wymuszamy "issuetype" w JSON
		private Issuetype issueType;
		private Description description;
		private Project project;
        private String summary;

        public Fields(String summary) {
			this.summary = summary;
		}

		@Override
		public String toString() {
			return "Fields [summary=" + summary + ", issueType=" + issueType + ", description=" + description + "]";
		}

        @Data
		public class Project
		{
            private String key;

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

        @Data
		public class Issuetype
		{
			String name;
			public Issuetype(String name) {
				this.name = name;
			}

			@Override
			public String toString() {
				return "Issuetype [name=" + name + "]";
			}
			
		}

        @Data
		public class Description
		{
            private String type;
            private Integer version;
			private List<Content> content;

			public Description(String type, Integer version) {
				this.type = type;
				this.version = version;
			}

			@Override
			public String toString() {
				return "Description [type=" + type + ", version=" + version + "]";
			}

			public Description() {
				super();
			}

            @Data
			public class Content
			{
                private String type;

				@JsonProperty("content")
				private List<ContentOfContent> contentOfContent;
				
				public List<ContentOfContent> getContentOfContent() {
					return contentOfContent;
				}

				public Content(String type, String text) {
					this.type = type;
				}

				public String getType() {
					return type;
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
