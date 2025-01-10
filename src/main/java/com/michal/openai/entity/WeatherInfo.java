package com.michal.openai.entity;

// An object in weather info in ChatGPT response

public class WeatherInfo {
	
	String location;
	Integer temperature;
	String measurementUnit;
	
	
	public WeatherInfo(String location, String measurementUnit) {
		this.location = location;
		this.measurementUnit = measurementUnit;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Integer getTemperature() {
		return temperature;
	}
	public void setTemperature(Integer temperature) {
		this.temperature = temperature;
	}
	public String getMeasurementUnit() {
		return measurementUnit;
	}
	public void setMeasurementUnit(String measurementUnit) {
		this.measurementUnit = measurementUnit;
	}
	
	

}
