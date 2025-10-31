package com.michal.openai.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.persistence.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "gpt_response")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
	@Column(name = "content", columnDefinition = "text")
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

        @Data
	    @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Choice {

            @Transient
            private Integer index;

            @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
            @JoinColumn(name = "response_id")
            private GptMessage message;

            @JsonProperty("finish_reason")
            @Transient
            private String finishReason;

            @Override
            public String toString() {
                return "Choice [index=" + index + ", message=" + message + ", finishReason=" + finishReason + "]";
            }
            public Choice() {
                super();
            }
        }

        @Data
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

            @Override
            public String toString() {
                return "Usage [promptTokens=" + promptTokens + ", completionTokens=" + completionTokens + ", totalTokens="
                        + totalTokens + "]";
            }

        }

    @Override
	public String toString() {
		return "GptResponse [id=" + id + ", requestId=" + requestId + ", requestSlackID=" + requestSlackID
				+ ", content=" + content + ", object=" + object + ", created=" + created + ", model=" + model
				+ ", choices=" + choices + ", usage=" + usage + "]";
	}
}