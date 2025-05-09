package com.michal.openai.entity;

public class WeatherParameterProperties implements ParameterProperties {
	
	private Location location;
	private MeasurementUnit measurementUnit;
	
	public class Location
	{
		String type;
		String description;
		
		public Location(String type, String description) {
			this.type = type;
			this.description = description;
		}
		
		public Location() {}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getLocation() {
			return description;
		}

		public void setLocation(String location) {
			this.description = location;
		} 
	}
	
	public class MeasurementUnit {
		
		private String type;
		private String description;
		public String[] enumValues;
		
		public static final String CELSIUS = "celsius"; 
		public static final String FAHRENHEIT = "fahrenheit";
		
		public MeasurementUnit(String type, String description, String[] enumValues) {
			this.type = type;
			this.description = description;
			this.enumValues = enumValues;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String[] getEnumValues() {
			return enumValues;
		}

		public void setEnumValues(String[] enumValues) {
			this.enumValues = enumValues;
		}

		public static String getCelsius() {
			return CELSIUS;
		}

		public static String getFahrenheit() {
			return FAHRENHEIT;
		}
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public MeasurementUnit getMeasurementUnit() {
		return measurementUnit;
	}

	public void setMeasurementUnit(MeasurementUnit measurementUnit) {
		this.measurementUnit = measurementUnit;
	}
	
	
}
