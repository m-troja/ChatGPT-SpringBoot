package com.michal.openai;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.michal.openai.entity.GptFunction;
import com.michal.openai.entity.JiraCreateIssueParameterProperties;
import com.michal.openai.entity.WeatherParameterProperties;
import com.michal.openai.entity.WeatherParameterProperties.MeasurementUnit;
import com.michal.openai.functions.Function;
import com.michal.openai.functions.impl.CreateIssueFunction;
import com.michal.openai.functions.impl.GetAllJiraIssues;
import com.michal.openai.functions.impl.GetWeatherInfoFunction;
import com.michal.openai.functions.impl.SendEmailFunction;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.michal.openai.entity.SendEmailParameterProperties;

@Configuration
public class BeansConfiguration {
	
	@Value("${slack.bot.oauth.token}")
	private String slackSecurityTokenBot ;

  @Bean
    public ObjectMapper objectMapper() {
        // Create and configure the ObjectMapper to use snake_case naming strategy
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return objectMapper;
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
	
	@Bean("createJiraIssueCall")
	public Function createJiraIssueCall() {
		return new CreateIssueFunction();
	}
	
	@Bean("sendEmailFunction")
	public Function sendEmailCall() {
		return new SendEmailFunction();
	}
	
	@Bean("gptSendEmailFunction")
	public GptFunction gptSendEmailFunction(@Value("${gpt.function.gmail.send.email.name}") String functionName,
			@Value("${gpt.function.gmail.send.email.name}") String description, 
			@Value("${gpt.function.gmail.send.email.attr.addressee.email.desc}") String addresseeEmailAttrDescription, 
			@Value("${gpt.function.gmail.send.email.attr.addressee.name.desc}") String addresseeNameAttrDescription, 
			@Value("${gpt.function.gmail.send.email.attr.content.desc}") String contentAttrDescription,
			@Value("${gpt.function.gmail.send.email.attr.subject.desc}") String subjectAttrDescription) {
		var function = new GptFunction();
		function.setName(functionName);
		function.setDescription(description);
		GptFunction.Parameters parameters = function.new Parameters();
		parameters.setType("object");

		SendEmailParameterProperties properties = new SendEmailParameterProperties();
		properties.setAddresseeEmail(properties.new AddresseeEmail("string", addresseeEmailAttrDescription));
		properties.setAddresseeName(properties.new AddresseeName("string", addresseeNameAttrDescription));
		properties.setContent(properties.new Content("string", contentAttrDescription));
		properties.setSubject(properties.new Subject("string", subjectAttrDescription));

		parameters.setProperties(properties);
		parameters.setRequired(new String[] {"content", "subject"});
		function.setParameters(parameters);
		return function;
	}
	
	// Define  weather fnc behavior and its parameters
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
	
	@Bean("defineCreateJiraIssueFunction")
	public GptFunction defineCreateJiraIssueFunction(
			@Value("${gpt.function.jira.create.issue.name}") String functionName,
			@Value("${gpt.function.jira.create.issue.description}") String description, 
			@Value("${gpt.function.jira.create.issue.attr.description.desc}") String descrAttrDescription, 
			@Value("${gpt.function.jira.create.issue.attr.issuetype.desc}") String issueTypeAttrDescription, 
			@Value("${gpt.function.jira.create.issue.attr.issuetype.epic}") String epicIssueType, 
			@Value("${gpt.function.jira.create.issue.attr.issuetype.story}") String storyIssueType, 
			@Value("${gpt.function.jira.create.issue.attr.issuetype.task}") String taskIssueType, 
			@Value("${gpt.function.jira.create.issue.attr.issuetype.bug}") String bugIssueType,
			@Value("${gpt.function.jira.create.issue.attr.duedate.desc}") String dueDateAttrDescription, 
			@Value("${gpt.function.jira.create.issue.attr.summary.format}") String dueDateFormat, 
			@Value("${gpt.function.jira.create.issue.attr.summary.desc}") String summaryAttrDescription
			) 
	{
		var gptFunction = new GptFunction();
		gptFunction.setName(functionName);
		gptFunction.setDescription(description);

		// Predefine function parameters
		JiraCreateIssueParameterProperties properties = new JiraCreateIssueParameterProperties();
		properties.setDescription(properties.new Description("string",descrAttrDescription));
		properties.setSummary(properties.new Summary("string",summaryAttrDescription));
		properties.setDuedate(properties.new DueDate("string",dueDateAttrDescription, dueDateFormat));
		properties.setIssueType(properties.new IssueType("string",issueTypeAttrDescription, new String[] {epicIssueType, storyIssueType, taskIssueType, bugIssueType} ));
		
		// Define function's parameters
		GptFunction.Parameters parameters = gptFunction.new Parameters();
		parameters.setType("object");
		parameters.setProperties(properties);
		parameters.setRequired(new String[] {"summary", "description", "issuetype"});
		
		gptFunction.setParameters(parameters);

		return gptFunction;
	}
	
	@Bean("defineAllJiraIssuesFunction")
	public GptFunction defineGetAllJiraIssuesFunction() {
		var gptFunction = new GptFunction();
		gptFunction.setName("getAllJiraIssuesFunction");
		gptFunction.setDescription("Get basic data about all issues in my Jira: key, assignee, description, summary, due date.");

		return gptFunction;
	}
	
	@Bean("slackBotClient")
	public MethodsClient slackMethodClientBot() {
		return Slack.getInstance().methods(slackSecurityTokenBot);
	}


}
