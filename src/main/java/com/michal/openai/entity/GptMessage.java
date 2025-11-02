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
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GptMessage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Transient
    @Column(name = "content", columnDefinition = "text")
	private String content;

    @JsonProperty("user_name")
    @JsonIgnore
	private String userName; // Just to assign author to message in DB -> for context setting

    @JsonProperty("tool_calls")
    @Transient
	private List<Tool> toolCalls;
	@Transient
	private String name;
	@Transient
	private String role; 
	
	public GptMessage() {}

	public GptMessage(String role, String content, String userName) {
		this.role = role;
		this.content = content;
		this.userName = userName;
	}
	
	public GptMessage(String role, String content) {
		this.role = role;
		this.content = content;
	}

    @Data
	public static class Tool {
		String id;
		String type;
		@JsonProperty("function")
		FunctionCall functionCall;

        @Data
        public static class FunctionCall {
            String name;
            String arguments;
        }
	}
}
