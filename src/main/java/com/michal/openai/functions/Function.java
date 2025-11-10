package com.michal.openai.functions;

import java.io.IOException;

public interface Function {
	
	String execute(String arguments) throws IOException;

}
