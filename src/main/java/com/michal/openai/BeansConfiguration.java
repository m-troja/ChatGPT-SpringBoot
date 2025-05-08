package com.michal.openai;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.michal.openai.entity.GptFunction;
import com.michal.openai.entity.WeatherParameterProperties;
import com.michal.openai.entity.WeatherParameterProperties.MeasurementUnit;
import com.michal.openai.functions.Function;
import com.michal.openai.functions.impl.GetAllJiraIssues;
import com.michal.openai.functions.impl.GetWeatherInfoFunction;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;

@Configuration
public class BeansConfiguration {
	
	@Value("${slack.bot.oauth.token}")
	private String slackSecurityTokenBot ;

	@Bean
	public Gson gson() {
		
		return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	}
	
	@Bean
	public HttpClient httpClient() {
		return HttpClientBuilder.create().build();
	}
	
	@Bean("getWeatherInfoFunction")
	public Function weatherFunctionCall() {
		return new GetWeatherInfoFunction();
	}
	
	@Bean("getAllJiraIssuesFunction")
	public Function allJiraIssuesFunctionCall() {
		return new GetAllJiraIssues();
	}
	
	
	@Bean("gptWeatherFunction")
	public GptFunction defineGetWeatherFunction() {
		var gptFunction = new GptFunction();
		gptFunction.setName("getWeatherInfoFunction");
		gptFunction.setDescription("Get weather in specified location");
		
		GptFunction.Parameters gptFunctionParameters = gptFunction.new Parameters();
		gptFunctionParameters.setType("object");		
		gptFunctionParameters.setRequired(new String[] {"location"});
		
		WeatherParameterProperties weatherParameterProperties = new WeatherParameterProperties();
		weatherParameterProperties.setLocation(weatherParameterProperties.new Location("string", "Warsaw"));
		weatherParameterProperties.setMeasurementUnit(weatherParameterProperties.new MeasurementUnit("string", "Celsius or fahrenheit. Temperature measurement unit", 
				new String[] {MeasurementUnit.CELSIUS, MeasurementUnit.FAHRENHEIT}));
		
		gptFunctionParameters.setProperties(weatherParameterProperties);
		gptFunction.setParameters(gptFunctionParameters);
		return gptFunction;
	}
	
	@Bean("allJiraIssuesFunction")
	public GptFunction defineGetAllJiraIssuesFunction() {
		var gptFunction = new GptFunction();
		gptFunction.setName("getAllJiraIssuesFunction");
		gptFunction.setDescription("Get basic data about all issues in my Jira: key, assignee, description, summary, due date.");
		
//		GptFunction.Parameters gptFunctionParameters = gptFunction.new Parameters();
//		gptFunctionParameters.setType("object");		
//		gptFunctionParameters.setRequired(new String[] {"location"});
//		
//		WeatherParameterProperties weatherParameterProperties = new WeatherParameterProperties();
//		weatherParameterProperties.setLocation(weatherParameterProperties.new Location("string", "Warsaw"));
//		weatherParameterProperties.setMeasurementUnit(weatherParameterProperties.new MeasurementUnit("string", "Celsius or fahrenheit. Temperature measurement unit", 
//				new String[] {MeasurementUnit.CELSIUS, MeasurementUnit.FAHRENHEIT}));
//		
//		gptFunctionParameters.setProperties(weatherParameterProperties);
//		gptFunction.setParameters(gptFunctionParameters);
		
		return gptFunction;
	}
	
	@Bean("slackBotClient")
	public MethodsClient slackMethodClientBot() {
		return Slack.getInstance().methods(slackSecurityTokenBot);
	}
}
