package com.michal.openai.gpt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GptResponse {

	private String object;
	private Long created;
	private String model;
	private List<Choice> choices;
	private Usage usage;

        @Data
	    @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Choice {

            private Integer index;
            private GptMessage message;

            @JsonProperty("finish_reason")
            private String finishReason;

            public Choice() {
                super();
            }
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Usage {

            @JsonProperty("prompt_tokens")
            private Integer promptTokens;

            @JsonProperty("completion_tokens")
            private Integer completionTokens;

            @JsonProperty("total_tokens")
            private Integer totalTokens;
        }
}