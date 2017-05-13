package com.capgemini.hackathon.device.simulation.bo;

import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.capgemini.hackathon.device.simulation.model.EmergencyRequest;
import com.capgemini.hackathon.device.simulation.model.Location;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;

public class Car extends Vehicle {
	
	private boolean gotSome = false;

	public Car(DeviceClientConfig deviceClientConfig, Object id) {
		super(deviceClientConfig, id);
	}

	@Override
	public void process() {
		while (true) {
			driveToDestination(Location.createRandomLocation());
		}

	}

	@Override
	protected void addMetainformationWhenPublishLocation(JsonObject event) {
		// nothing to add
	}

	@Override
	protected void configureDeviceClient(DeviceClient deviceClient) {
	}
}
