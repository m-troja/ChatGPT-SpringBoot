package com.michal.openai.gpt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.michal.openai.functions.entity.GptFunction;
import com.michal.openai.functions.entity.GptTool;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GptRequest {

	@JsonIgnore
	private String authorRealname;

	private  String model;
	private List<GptMessage> messages;
	private Integer n;
	private Double temperature;

    @JsonProperty("max_completion_tokens")
	private Integer maxOutputTokens;

	private boolean Stream;

// Not supported by GPT-5.1
//    @JsonProperty("presence_penalty")
//    @Transient
//	private Double presencePenalty;
//
//    @JsonProperty("frequency_penalty")
//    @Transient
//	private Double frequencyPenalty;

    @JsonProperty("top_p")
	private Double topP;

	private String stop;

    @JsonProperty("logit_bias")
	private Map<String, Integer> logitBias;

	@JsonIgnore
	private List<GptFunction> functions;

	private List<GptTool> tools;

	@JsonProperty("tool_choice")
	String toolChoice;


}
