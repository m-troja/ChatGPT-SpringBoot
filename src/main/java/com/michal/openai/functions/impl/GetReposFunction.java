package com.michal.openai.functions.impl;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.functions.Function;
import com.michal.openai.github.GithubService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetReposFunction implements Function {
	
	@Autowired
	GithubService githubService;
	
	@Override
	public CompletableFuture<String> execute(String loginJson) {
        log.info("Execute githubService with login Json: {}", loginJson);
		
		String login = "";
		try 
		{ 
			ObjectMapper mapper = new ObjectMapper();
	        JsonNode root = mapper.readTree(loginJson);
	        login = root.get("login").asText();
            log.debug("Extracted login : {}", login);
		}
		catch (Exception e)
		{
			log.error("Error extracting loginJson");
		}
		
		try {
			return githubService.getUserReposWithBranches(login);
		} catch (IOException e) {

            log.error("Failed to fetch repositories for user: {}", loginJson, e);
	        return CompletableFuture.failedFuture(e);
		}

	}

}
