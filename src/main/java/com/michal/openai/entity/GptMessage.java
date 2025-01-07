package com.michal.openai.entity;

public class GptMessage {
	
	String name;
	String role;
	String content;
	
	public GptMessage() {}

	public GptMessage(String role, String content) {
		this.role = role;
		this.content = content;
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
	
	

}
