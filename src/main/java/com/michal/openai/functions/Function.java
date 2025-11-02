package com.michal.openai.functions;

import java.util.concurrent.CompletableFuture;

public interface Function {
	
	String execute(String arguments);

}
