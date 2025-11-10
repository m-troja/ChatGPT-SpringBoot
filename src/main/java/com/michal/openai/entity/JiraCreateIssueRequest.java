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
	public static class Fields
	{
        @JsonProperty("issuetype")
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
		public static class Project
		{
            private String key;

			public Project() {
			}

		}	//END project

        public record Issuetype(String name) {}

        @Data
		public static class Description
		{
            private String type;
            private Integer version;
			private List<Content> content;

			public Description() {
				super();
			}

            @Data
			public static class Content
			{
                private String type;

				@JsonProperty("content")
				private List<ContentOfContent> contentOfContent;

				public Content() {
					super();
				}

                @Data
				public static class ContentOfContent
				{
					String type;
					String text;
					public ContentOfContent() {
						super();
					}
				}
			} //END CONTENT 1
		} // END description
	}
}
