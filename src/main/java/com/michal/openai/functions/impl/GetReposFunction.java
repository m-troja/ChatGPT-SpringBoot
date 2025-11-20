package com.michal.openai.functions.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.functions.Function;
import com.michal.openai.github.GithubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetReposFunction implements Function {
	
    private final GithubService githubService;
	private final ObjectMapper objectMapper;

    @Override
	public String execute(String loginJson) {
        log.info("Execute GetReposFunction with login: {}", loginJson);
		
		String login;
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
            throw new RuntimeException("Error extracting loginJson");
		}
        String answer;
        try {
            answer = objectMapper.writeValueAsString(githubService.getUserReposWithBranches(login));
            return answer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
