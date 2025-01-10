package com.michal.openai.entity;

import java.util.List;

public class GptMessage {
	
	String name;
	String role;
	String content;
	FunctionCall functionCall;
	
	public GptMessage() {}

	public GptMessage(String role, String content) {
		this.role = role;
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FunctionCall getFunctionCall() {
		return functionCall;
	}

	public void setFunctionCall(FunctionCall functionCall) {
		this.functionCall = functionCall;
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
		return "GptMessage [name=" + name + ", role=" + role + ", content=" + content + "]";
	}
	
	public class FunctionCall {
		
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
	}

}
