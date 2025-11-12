package com.michal.openai.github.entity;

import com.michal.openai.functions.entity.ParameterProperties;
import com.michal.openai.functions.entity.ParameterPropertyAttribute;
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
