package com.michal.openai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${github.token}")
    private String githubToken;
    @Value("${github.api.url}")
    private String githubBaseApiUrl;
	@Value("${gpt.chat.api.key}")
	private String chatGptApiKey;
	@Value("${gpt.chat.api.url}")
	private String chatGptApiUrl;

    @Bean("githubRestClient")
    public RestClient githubRestClient()
    {

    	 RestClient.Builder builder = RestClient.builder()
    			.baseUrl(githubBaseApiUrl)
    			.defaultHeader("Accept", "application/json");
    	
    	        if (githubToken != null && !githubToken.isBlank()) {
    	        	builder.defaultHeader("Authorization",  "Bearer " + githubToken);
    	        }
    	        
			return builder.build();
	}
    
    @Bean("gptRestClient")
    public RestClient gptRestClient()
    {
    	 RestClient.Builder builder = RestClient.builder()
    			.baseUrl(chatGptApiUrl)
    			.defaultHeader("Content-Type", "application/json")
    	        .defaultHeader("Authorization",  "Bearer " + chatGptApiKey);
    	        
			return builder.build();
	}

    @Bean("taskSystemRestClient")
    public RestClient taskSystemRestClient()
    {
        String taskSystemHost = System.getenv().getOrDefault("TS_HOST", "localhost");
        String taskSystemPort = System.getenv().getOrDefault("TS_HTTP_PORT", "6901");
        String taskSystemUrl = String.format("http://%s:%s", taskSystemHost, taskSystemPort);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(taskSystemUrl)
                .defaultHeader("Content-Type", "application/json");

        return builder.build();
    }
}
