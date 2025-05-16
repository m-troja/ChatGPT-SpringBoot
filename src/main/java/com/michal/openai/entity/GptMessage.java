package com.michal.openai.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class GptMessage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Transient
	private String content;
	
	@JsonIgnore
	private String userName; // Just to assign author to message in DB -> for context setting
	
	@Transient
	private List<Tool> toolCalls;
	@Transient
	private String name;
	@Transient
	private String role; 
	
	public List<Tool> getToolCalls() {
		return toolCalls;
	}

	public void setToolCalls(List<Tool> toolCalls) {
		this.toolCalls = toolCalls;
	}

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
	

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	
	
	@Override
	public String toString() {
		return "GptMessage [name=" + name + ", role=" + role + ", content=" + content + ", userName=" + userName
				+ ", toolCalls=" + toolCalls + "]";
	}



	public static class Tool {
		String id;
		String type;
		@JsonProperty("function")
		FunctionCall function;
		public Tool() {
			super();
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public FunctionCall getFunctionCall() {
			return function;
		}
		public void setFunctionCall(FunctionCall function) {
			this.function = function;
		}
		
		
	
	@Override
		public String toString() {
			return "Tool [id=" + id + ", type=" + type + ", function=" + function + "]";
		}



	public static class FunctionCall {
		
		String name;
		
		String arguments;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getArguments() {
			return arguments;
		}
		public void setArguments(String arguments) {
			this.arguments = arguments;
		}
		@Override
		public String toString() {
			return "FunctionCall [name=" + name + ", arguments=" + arguments + "]";
		}
		
	}
	}

}
