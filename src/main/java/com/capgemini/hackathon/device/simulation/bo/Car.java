package com.capgemini.hackathon.device.simulation.bo;

import com.capgemini.hackathon.device.simulation.ApplicationClientConfig;
import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.capgemini.hackathon.device.simulation.model.Location;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.device.DeviceClient;

public class Car extends Vehicle {

	public Car(DeviceClientConfig deviceClientConfig, ApplicationClientConfig appClientConfig) {
		super(deviceClientConfig, appClientConfig);
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
		// nothing to do
	}

	@Override
	protected void configureAppplicationClient(ApplicationClient applicationClient) {
		// nothing to do
	}
}