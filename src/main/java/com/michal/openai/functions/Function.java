package com.michal.openai.functions;

import java.util.concurrent.CompletableFuture;

public interface Function {
	
	CompletableFuture<String> execute(String arguments);

}
