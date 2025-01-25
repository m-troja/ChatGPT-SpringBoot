package com.michal.openai.entity;

public class SlackRequestData {
	
	String messageAuthorId;
	String message;
	String channelIdFrom;
	
	public SlackRequestData(String messageAuthorId, String message, String channelIdFrom) {
		super();
		this.messageAuthorId = messageAuthorId;
		this.message = message;
		this.channelIdFrom = channelIdFrom;
	}
	
	public String getMessageAuthorId() {
		return messageAuthorId;
	}
	
	public void setMessageAuthorId(String messageAuthorId) {
		this.messageAuthorId = messageAuthorId;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getChannelIdFrom() {
		return channelIdFrom;
	}
	
	public void setChannelIdFrom(String channelIdFrom) {
		this.channelIdFrom = channelIdFrom;
	}
	

}
