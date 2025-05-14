package com.michal.openai.entity;

import java.util.List;

import java.util.Map;

import jakarta.persistence.FetchType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
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

	@Transient
	private  String model;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "request_message", 
		joinColumns = @JoinColumn( 
				name = "request_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "message_id", referencedColumnName = "id"))
	private List<GptMessage> messages;
	@Transient
	private Integer n;
	@Transient
	private Double temperature;
	@Transient
	private Integer maxTokens;
	@Transient
	private boolean Stream;
	@Transient
	private Double presencePenalty;
	@Transient
	private Double frequencyPenalty;
	@Transient
	private Double topP;
	@Transient
	private String stop;
	@Transient
	private Map<String, Integer> logitBias;
	@Transient
	private String user;
	@Transient
	@JsonIgnore
	private List<GptFunction> functions;
	@Transient
	private List<GptTool> tools;
	
	@JsonProperty("tool_choice")  // for correct key in JSON file
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
	public Integer getMaxTokens() {
		return maxTokens;
	}
	public void setMaxTokens(Integer maxTokens) {
		this.maxTokens = maxTokens;
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
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "GptRequest [model=" + model + ", messages=" + messages + ", n=" + n + ", temperature=" + temperature
				+ ", maxTokens=" + maxTokens + ", Stream=" + Stream + ", presencyPenalty=" + presencePenalty
				+ ", frequencyPenalty=" + frequencyPenalty + ", topP=" + topP + ", stop=" + stop + ", logitBias="
				+ logitBias + ", user=" + user + "]";
	}
	
	

}
