package com.michal.openai.jira.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.michal.openai.entity.JiraIssue;
import com.michal.openai.jira.JiraService;

@Service
public class DefaultJiraService implements JiraService {
	
	@Value("${jira.url}")
	private String url;
	
	@Value("${jira.key}")
	private String key;
	
	@Value("${jira.issue}")
	private String issue;
	
	@Value("${jira.project.name}")
	private String projectName;
	
	@Value("${jira.search}")
	private String search;
	
	@Value("${jira.maxresults}")
	private String maxresults;
	
	@Autowired
	HttpClient httpClient;
	
	@Autowired
	Gson gson;
	
	public String getIssueJson(String id)
	{
		String urlToGet = url + issue + "/" + projectName + "-" + id	;	
		HttpGet httpGet = new HttpGet(urlToGet);
		
		httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + key);
		httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String responseBody = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

	        return responseBody;
		}
		
	    catch (IOException e) {
	        e.printStackTrace();
	    }
		
		return null;
	}

	public List<JiraIssue> getIssues()
	{
		String urlToGet = url + search + "?jql=project=" + projectName 	;	
		System.out.println(urlToGet);
		HttpGet httpGet = new HttpGet(urlToGet);
		
		httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + key);
		httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		httpGet.setHeader(HttpHeaders.ACCEPT, "application/json");

		List<JiraIssue> issues = new ArrayList<>();
		
		// Represent response string as JSON object
		
		try 
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
			
			// JsonObject contains all issues
			JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
			
			// JsonArrayOfIssues contains array of all returned issues
			JsonArray jsonArrayOfIssues = jsonObject.getAsJsonArray("issues");
			
			// For every issue in jsonArrayOfIssues -> extract fields to issue's object
			for (JsonElement element : jsonArrayOfIssues)
			{
				// IssueObject is JSON representation of issue
				JsonObject issueObject = element.getAsJsonObject();
				
				// Create jiraIssue object and assign key to it (it's before fields)
				JiraIssue jiraIssue = new JiraIssue();
				jiraIssue.setKey(issueObject.get("key").getAsString());
			
				// Get object of issue's fields
				JsonObject fieldsJsonObject = issueObject.getAsJsonObject("fields");

				if (fieldsJsonObject.has("summary") && !fieldsJsonObject.get("summary").isJsonNull()) 
		        {
		                jiraIssue.setSummary(fieldsJsonObject.get("summary").getAsString());
		        }

				//	Get issueType object from fieldsObject, then extract description from issueType
		        if (fieldsJsonObject.has("issuetype") && !fieldsJsonObject.get("issuetype").isJsonNull()) {
		            JsonObject issuetypeObject = fieldsJsonObject.getAsJsonObject("issuetype");
		            
		            if (issuetypeObject.has("description") && !issuetypeObject.get("description").isJsonNull() ) {
		                // Safely get the description as a string
		               jiraIssue.setDescription(issuetypeObject.get("description").getAsString());
		            } 
		        }
		        
		        // If one of issue's field is assignee and is not null
		        if (fieldsJsonObject.has("assignee") && !fieldsJsonObject.get("assignee").isJsonNull()) 
		        {
		            JsonObject assigneeJsonObject = fieldsJsonObject.getAsJsonObject("assignee");
		            
		            // If assignee has displayName -> assign assignee to object
		            if (assigneeJsonObject.has("displayName") && !assigneeJsonObject.get("displayName").isJsonNull()) {
		                jiraIssue.setAssignee(assigneeJsonObject.get("displayName").getAsString());
		            }
		        }
		        
		        // If one of issue's field is duedate and is not null
		        if (fieldsJsonObject.has("duedate") && !fieldsJsonObject.get("duedate").isJsonNull()) 
		        {
		                jiraIssue.setDuedate(fieldsJsonObject.get("duedate").getAsString());
		        }
		        
		        issues.add(jiraIssue);	
		        System.out.println(jiraIssue.toString());
			}
			return issues;
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null; 
	} 
	
}
