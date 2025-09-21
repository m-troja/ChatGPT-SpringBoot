package com.michal.openai.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "gpt_response", schema = "chatgpt-integration") 
public class GptResponse {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;
	
	@JsonIgnore
	@Column(name = "request_id")
	private Long requestId; // for DB column
	
	@JsonIgnore
	@Column(name = "request_author_slackid")
	private String requestSlackID; // for DB column
	
	@JsonIgnore
	@Column(name = "content")
	private String content; // for DB column
	
	@Transient
	private String object;
	@Transient
	private Long created;
	@Transient
	private String model;
	@Transient
	private List<Choice> choices;
	@Transient
	private Usage usage;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public Long getCreated() {
		return created;
	}
	public void setCreated(Long created) {
		this.created = created;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public List<Choice> getChoices() {
		return choices;
	}
	public void setChoices(List<Choice> choices) {
		this.choices = choices;
	}
	public Usage getUsage() {
		return usage;
	}
	public void setUsage(Usage usage) {
		this.usage = usage;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Choice {
		
		@Transient
		private Integer index;
		
		@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
		@JoinColumn(name = "response_id")  // Analogicznie, Hibernate doda response_id
		private GptMessage message;

        @JsonProperty("finish_reason")
        @Transient
		private String finishReason;
		
		public Integer getIndex() {
			return index;
		}
		public void setIndex(Integer index) {
			this.index = index;
		}
		public GptMessage getMessage() {
			return message;
		}
		public void setMessage(GptMessage message) {
			this.message = message;
		}
		public String getFinishReason() {
			return finishReason;
		}
		public void setFinishReason(String finishReason) {
			this.finishReason = finishReason;
		}
		@Override
		public String toString() {
			return "Choice [index=" + index + ", message=" + message + ", finishReason=" + finishReason + "]";
		}
		public Choice() {
			super();
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Usage {

        @JsonProperty("prompt_tokens")
        @Transient
		private Integer promptTokens;

        @JsonProperty("completion_tokens")
        @Transient
		private Integer completionTokens;

        @JsonProperty("total_tokens")
        @Transient
		private Integer totalTokens;
		
		public Integer getPromptTokens() {
			return promptTokens;
		}
		public void setPromptTokens(Integer promptTokens) {
			this.promptTokens = promptTokens;
		}
		public Integer getCompletionTokens() {
			return completionTokens;
		}
		public void setCompletionTokens(Integer completionTokens) {
			this.completionTokens = completionTokens;
		}
		public Integer getTotalTokens() {
			return totalTokens;
		}
		public void setTotalTokens(Integer totalTokens) {
			this.totalTokens = totalTokens;
		}
		@Override
		public String toString() {
			return "Usage [promptTokens=" + promptTokens + ", completionTokens=" + completionTokens + ", totalTokens="
					+ totalTokens + "]";
		}
		
	}
	public Long getRequestId() {
		return requestId;
	}
	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}
	public String getRequestSlackID() {
		return requestSlackID;
	}
	public void setRequestSlackID(String requestSlackID) {
		this.requestSlackID = requestSlackID;
	}
	@Override
	public String toString() {
		return "GptResponse [id=" + id + ", requestId=" + requestId + ", requestSlackID=" + requestSlackID
				+ ", content=" + content + ", object=" + object + ", created=" + created + ", model=" + model
				+ ", choices=" + choices + ", usage=" + usage + "]";
	}

	
	
}