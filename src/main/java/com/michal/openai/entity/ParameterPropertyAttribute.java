package com.michal.openai.entity;

import lombok.Data;

@Data
public abstract class ParameterPropertyAttribute {

    private String type;
	private String description;
	public ParameterPropertyAttribute() { }
	public ParameterPropertyAttribute(String type, String description) {
		this.type = type;
		this.description = description;
	}
}
