package com.michal.openai.entity;

import java.util.List;

import java.util.Map;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "gpt_request", schema = "chatgpt-integration")  
public class GptRequest {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private  Long id;
	
	@JsonIgnore
	@Column(name = "content")
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
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<GptTool> getTools() {
		return tools;
	}

	public void setTools(List<GptTool> tools) {
		this.tools = tools;
	}

	public String getToolChoice() {
		return toolChoice;
	}

	public void setToolChoice(String toolChoice) {
		this.toolChoice = toolChoice;
	}

	public String getModel() {
		return model;
	}
	
	public List<GptFunction> getFunctions() {
		return functions;
	}

	public String getAuthorRealname() {
		return authorRealname;
	}

	public void setAuthorRealname(String authorRealname) {
		this.authorRealname = authorRealname;
	}

	public void setFunctions(List<GptFunction> functions) {
		this.functions = functions;
	}

	public void setModel(String model) {
		this.model = model;
	}
	public List<GptMessage> getMessages() {
		return messages;
	}
	public void setMessages(List<GptMessage> messages) {
		this.messages = messages;
	}
	public Integer getN() {
		return n;
	}
	public void setN(Integer n) {
		this.n = n;
	}
	public Double getTemperature() {
		return temperature;
	}
	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}
	public Integer getMaxOutputTokens() {
		return maxOutputTokens;
	}
	public void setMaxOutputTokens(Integer maxOutputTokens) {
		this.maxOutputTokens = maxOutputTokens;
	}
	public boolean isStream() {
		return Stream;
	}
	public void setStream(boolean stream) {
		Stream = stream;
	}
	public Double getPresencePenalty() {
		return presencePenalty;
	}
	public void setPresencePenalty(Double presencyPenalty) {
		this.presencePenalty = presencyPenalty;
	}
	public Double getFrequencyPenalty() {
		return frequencyPenalty;
	}
	public void setFrequencyPenalty(Double frequencyPenalty) {
		this.frequencyPenalty = frequencyPenalty;
	}
	public Double getTopP() {
		return topP;
	}
	public void setTopP(Double topP) {
		this.topP = topP;
	}
	public String getStop() {
		return stop;
	}
	public void setStop(String stop) {
		this.stop = stop;
	}
	public Map<String, Integer> getLogitBias() {
		return logitBias;
	}
	public void setLogitBias(Map<String, Integer> logitBias) {
		this.logitBias = logitBias;
	}
	public String getUser() {
		return author;
	}
	public void setUser(String user) {
		this.author = user;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String toString() {
		return "GptRequest [id=" + id + ", content=" + content + ", model=" + model + ", messages=" + messages + ", n="
				+ n + ", temperature=" + temperature + ", maxOutputToken=" + maxOutputTokens + ", Stream=" + Stream
				+ ", presencePenalty=" + presencePenalty + ", frequencyPenalty=" + frequencyPenalty + ", topP=" + topP
				+ ", stop=" + stop + ", logitBias=" + logitBias + ", author=" + author + ", authorRealname="
				+ authorRealname + ", functions=" + functions + ", tools=" + tools + ", toolChoice=" + toolChoice + "]";
	}

	
	

}
