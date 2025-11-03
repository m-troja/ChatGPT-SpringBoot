package com.michal.openai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class GetReposParameterProperties implements ParameterProperties {

	private LoginAttr login;

	public void setLogin(LoginAttr login) {
		this.login = login;
	}

    @Data
    @AllArgsConstructor
	public class LoginAttr extends ParameterPropertyAttribute
	{
		private String type;
		private String description;
	}
}
