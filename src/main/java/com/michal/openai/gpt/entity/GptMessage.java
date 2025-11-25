package com.michal.openai.gpt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GptMessage {

    private int id;
    private String content;

    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    private String name;
    private String role;

    public GptMessage() {}

    public GptMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // ----------------------- ToolCall -----------------------
    @Data
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public static class ToolCall {

        private Long internalId;
        private String id;
        private String type;

        @JsonProperty("function")
        private FunctionCall functionCall;

        public ToolCall(String id, String type) {
            this.id = id;
            this.type = type;
        }

        @Data
        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
        public static class FunctionCall {

            private String name;

            private String arguments;

            public FunctionCall() {}

            public FunctionCall(String name, String arguments) {
                this.name = name;
                this.arguments = arguments;
            }
        }
    }
}
