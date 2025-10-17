package com.michal.openai.entity;

import lombok.Data;

@Data
public class GptFunction {
	
	private String name;
	private String description;
	private Parameters parameters;

    @Data
	public class Parameters {
		
		private String type;
		private ParameterProperties properties;
        private String[] required;
	}
}


