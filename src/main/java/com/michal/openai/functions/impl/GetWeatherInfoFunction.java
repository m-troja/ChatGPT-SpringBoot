package com.michal.openai.functions.impl;

import java.lang.reflect.Type;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.michal.openai.entity.WeatherInfo;
import com.michal.openai.functions.Function;

public class GetWeatherInfoFunction implements Function {

	
	@Autowired
	Gson gson;
	
	@Override
	public String execute(String arguments) {
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		Map<String, String> argumentsMap = gson.fromJson(arguments, type);
		String location = argumentsMap.get("location");
		String unit = argumentsMap.get("unit");
		
		WeatherInfo weatherInfo = new WeatherInfo(location, unit);
		weatherInfo.setLocation(location);
		weatherInfo.setMeasurementUnit(unit);
		weatherInfo.setTemperature(23);
		return gson.toJson(weatherInfo);
	}

}
