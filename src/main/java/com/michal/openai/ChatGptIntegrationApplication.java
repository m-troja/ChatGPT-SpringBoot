package com.michal.openai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@Configuration
@PropertySources({
	@PropertySource("classpath:application.properties") ,  
	@PropertySource("classpath:secrets.properties")
	})
		
public class ChatGptIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatGptIntegrationApplication.class, args);
	}

}
