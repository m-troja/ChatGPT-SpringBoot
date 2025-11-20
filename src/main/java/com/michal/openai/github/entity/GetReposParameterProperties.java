package com.michal.openai.github.entity;

import com.michal.openai.functions.entity.ParameterProperties;
import com.michal.openai.functions.entity.ParameterPropertyAttribute;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class GetReposParameterProperties implements ParameterProperties {

	private LoginAttr login;

    @Data
    @EqualsAndHashCode(callSuper = true)
	public static class LoginAttr extends ParameterPropertyAttribute
	{
		private final String type;
		private final String description;
	}
}
