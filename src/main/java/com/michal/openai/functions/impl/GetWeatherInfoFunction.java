package com.michal.openai.functions.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.WeatherInfo;
import com.michal.openai.functions.Function;

@Slf4j
public class GetWeatherInfoFunction implements Function {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public CompletableFuture<String> execute(String arguments) {
        try {
            Map<String, String> argumentsMap = objectMapper.readValue(arguments, new TypeReference<Map<String, String>>() {});
            String location = argumentsMap.get("location");
            String unit = argumentsMap.get("unit");

            WeatherInfo weatherInfo = new WeatherInfo(location,23, "Celsius");
            return CompletableFuture.completedFuture("Temperature in " + weatherInfo.location() + " is " + weatherInfo.temperature() + " " + weatherInfo.measurementUnit());
        } catch (Exception e) {
            log.error(e.getMessage());
            return CompletableFuture.completedFuture("Error processing weather info.");
        }
    }
}
