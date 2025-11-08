package com.michal.openai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class GetReposParameterProperties implements ParameterProperties {

	private LoginAttr login;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
	public static class LoginAttr extends ParameterPropertyAttribute
	{
		private String type;
		private String description;
	}
}
