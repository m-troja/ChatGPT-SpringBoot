package com.michal.openai.entity;

public class GetReposParameterProperties implements ParameterProperties {

	private LoginAttr login;
	
	public LoginAttr getLogin() {
		return login;
	}

	public void setLogin(LoginAttr login) {
		this.login = login;
	}

	public class LoginAttr extends ParameterPropertyAttribute
	{
		private String type;
		private String description;
		
		public LoginAttr(String type, String description) {
			this.type = type;
			this.description = description;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
}
