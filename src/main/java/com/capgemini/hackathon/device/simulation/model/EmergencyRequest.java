package com.capgemini.hackathon.device.simulation.model;

import com.google.gson.JsonObject;

public class EmergencyRequest {
	private static final String CMD="cmd";
	public static final String EVENT="icrashed";
	private static final String LAT="latitude";
	private static final String LON="longitude";
	
	private double lat;
	private double lon;
	
	public EmergencyRequest(double lat,double lon) {
		// TODO Auto-generated constructor stub
		this.lat = lat;
		this.lon = lon;
	}
	
	public JsonObject asJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty(CMD, "emergency");
		json.addProperty(LAT, lat);
		json.addProperty(LON, lon);

		return json;
	}
}
