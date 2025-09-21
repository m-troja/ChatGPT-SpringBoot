package com.michal.openai.entity;

public class GptTool {
	
	String type;
	GptFunction function;
	
	public GptTool(String type, GptFunction function) {
		super();
		this.type = type;
		this.function = function;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public GptFunction getFunction() {
		return function;
	}

	public void setFunction(GptFunction function) {
		this.function = function;
	}

    @Override
    public String toString() {
        return "GptTool{" +
                "type='" + type + '\'' +
                ", function=" + function +
                '}';
    }
}
