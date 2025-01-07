package com.michal.openai;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class BeansConfiguration {
	
	@Bean
	public Gson gson() {
		
		return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	}
	
	@Bean
	public HttpClient httpClient() {
		return HttpClientBuilder.create().build();
	}

}
