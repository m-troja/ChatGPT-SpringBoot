package com.michal.openai.entity;

// An object in weather info in ChatGPT response

public record WeatherInfo(String location, Integer temperature, String measurementUnit) {}
