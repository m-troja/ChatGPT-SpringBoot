package com.michal.openai.log;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonSaver {
	FileWriter writer;

	String responsePath = Paths.get("C:", "tmp", "JSON", "response",  generateResponseFileName() ).toString();
	String requestPath = Paths.get("C:", "tmp", "JSON", "request",  generateRequestFileName() ).toString();

	
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
    //   String checkedFilename = checkFileExisting(originalFilename, path);
        		
        return originalFilename + ".json";
    }
	
//	private static String checkFileExisting(String originalFilename, String path) {
//		Path filePath = Paths.get(path, null)
//		return null;
//	}


	public void saveResponseJson(String responseBody)
	{
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		JsonElement je = JsonParser.parseString(responseBody);
		String responseJsonString = prettyGson.toJson(je);
		
		saveFile(responseJsonString, responsePath);
		
		System.out.println("Response saved to file: " + responsePath);
		System.out.println("Debug: " + prettyGson.toJson(je).toString());
	}
	
	public void saveGptRequestToJson(String requestBody)
	{
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		JsonElement je = JsonParser.parseString(requestBody);
		String requestJsonString = prettyGson.toJson(je);

		saveFile(requestJsonString, requestPath);

		System.out.println("Request saved to file: " + requestPath);
		System.out.println("Debug: " + prettyGson.toJson(je).toString());
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
