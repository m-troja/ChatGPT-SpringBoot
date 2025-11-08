package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "gpt_response")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GptResponse {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;

	 /*
	  * Used for DB columns & context
	  */
	@JsonIgnore
	@Column(name = "request_id")
	private Long requestId;
	@JsonIgnore
	@Column(name = "request_author_slackid")
	private String requestSlackID;
    @JsonIgnore
    @Column(name = "request_author_realname")
    private String requestAuthorRealName;
	@JsonIgnore
	@Column(name = "content", columnDefinition = "text")
    private String content;
    @JsonIgnore
    @Column(name = "isFunctionCall")
    private boolean isFunctionCall;

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
        }
}