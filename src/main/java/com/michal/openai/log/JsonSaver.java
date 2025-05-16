package com.michal.openai.log;

import java.io.FileWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSaver {
	FileWriter writer;

	String responsePath = Paths.get("C:", "tmp", "JSON", "response",  generateResponseFileName() ).toString();
	String requestPath = Paths.get("C:", "tmp", "JSON", "request",  generateRequestFileName() ).toString();
 
    private final ObjectMapper objectMapper = new ObjectMapper(); 

	private static String generateResponseFileName() {
		LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        
        String formattedDateTime = now.format(formatter);
		String originalFilename = "response_" + formattedDateTime;
		
        return originalFilename + ".json";
    }
	
	
	private static String generateRequestFileName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        
        String formattedDateTime = now.format(formatter);
        String originalFilename = "request_" + formattedDateTime;
        		
        return originalFilename + ".json";
    }
	

	public void saveResponseJson(String responseBody) throws JsonProcessingException
	{
		JsonNode jsonNode = objectMapper.readTree(responseBody);
		String responseJsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
		
		saveFile(responseJsonString, responsePath);
		
	//	System.out.println("Response saved to file: " + responsePath);
	//	System.out.println("Debug: " + prettyobjectMapper.writeValueAsString(je).toString());
	}
	
	public void saveGptRequestToJson(String requestBody) throws JsonProcessingException
	{
		JsonNode jsonNode = objectMapper.readTree(requestBody);
		String reqestJsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
		
		saveFile(reqestJsonString, requestPath);

	//	System.out.println("Request saved to file: " + requestPath);
	//	System.out.println("Debug: " + prettyobjectMapper.writeValueAsString(je).toString());
	}
	
	private void saveFile(String jsonString, String path)
	{
		
		try {
			writer = new FileWriter(path);
			writer.write(jsonString);
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
