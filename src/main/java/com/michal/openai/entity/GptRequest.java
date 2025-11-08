package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "gpt_request")
@Data
public class GptRequest {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private  Long id;
	
	@JsonIgnore
    @Column(name = "content", columnDefinition = "text")
    private String content;

	@JsonIgnore
	@Column(name = "author_slackid")
	private String author;

	@JsonIgnore
	@Column(name = "author_realname")
	private String authorRealname;
	
	@Transient
	private  String model;
	
	@Transient
	private List<GptMessage> messages;
	
	@Transient
	private Integer n;
	@Transient
	private Double temperature;

    @JsonProperty("max_tokens")
    @Transient
	private Integer maxOutputTokens;

	@Transient
	private boolean Stream;

    @JsonProperty("presence_penalty")
    @Transient
	private Double presencePenalty;

    @JsonProperty("frequency_penalty")
    @Transient
	private Double frequencyPenalty;

    @JsonProperty("top_p")
    @Transient
	private Double topP;

	@Transient
	private String stop;

    @JsonProperty("logit_bias")
    @Transient
	private Map<String, Integer> logitBias;

	@Transient
	@JsonIgnore
	private List<GptFunction> functions;

	@Transient
	private List<GptTool> tools;

	@JsonProperty("tool_choice")
	@Transient
	String toolChoice;

	@Override
	public String toString() {
		return "GptRequest [id=" + id + ", content=" + content + ", model=" + model + ", messages=" + messages + ", n="
				+ n + ", temperature=" + temperature + ", maxOutputToken=" + maxOutputTokens + ", Stream=" + Stream
				+ ", presencePenalty=" + presencePenalty + ", frequencyPenalty=" + frequencyPenalty + ", topP=" + topP
				+ ", stop=" + stop + ", logitBias=" + logitBias + ", author=" + author + ", authorRealname="
				+ authorRealname + ", functions=" + functions + ", tools=" + tools + ", toolChoice=" + toolChoice + "]";
	}
}
