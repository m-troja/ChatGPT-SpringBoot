package com.michal.openai.functions.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.WeatherInfo;
import com.michal.openai.functions.Function;

public class GetWeatherInfoFunction implements Function {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public CompletableFuture<String> execute(String arguments) {
        // Use Jackson's TypeReference instead of  's Type
        try {
            Map<String, String> argumentsMap = objectMapper.readValue(arguments, new TypeReference<Map<String, String>>() {});
            String location = argumentsMap.get("location");
            String unit = argumentsMap.get("unit");

            WeatherInfo weatherInfo = new WeatherInfo(location, unit);
            weatherInfo.setLocation(location);
            weatherInfo.setMeasurementUnit("Celsius");
            weatherInfo.setTemperature(23);

            return CompletableFuture.completedFuture("Temperature in " + weatherInfo.getLocation() + " is " + weatherInfo.getTemperature() + " " + weatherInfo.getMeasurementUnit());
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture("Error processing weather info.");
        }
    }
}
