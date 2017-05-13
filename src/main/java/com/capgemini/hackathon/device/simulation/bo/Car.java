package com.capgemini.hackathon.device.simulation.bo;

import java.util.*;

import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.capgemini.hackathon.device.simulation.bo.Vehicle.Interruption;
import com.capgemini.hackathon.device.simulation.model.Emergency;
import com.capgemini.hackathon.device.simulation.model.EmergencyRequest;
import com.capgemini.hackathon.device.simulation.model.Location;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.graphhopper.GHResponse;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;

public class Car extends Vehicle {
	private Map<String, List<Integer>> ambulanceRoutes; 
	private StupidInterruptSolution interrupter = new StupidInterruptSolution();
	private Location carDestination;

	public Car(DeviceClientConfig deviceClientConfig, Object id) {
		super(deviceClientConfig, id);
		this.ambulanceRoutes = new HashMap<>();
	}

	@Override
	public void process() {
		this.carDestination = Location.createRandomLocation();
		while (true) {
			this.interrupter.setInterrupt(false);
			driveToDestination(this.carDestination);
		}

	}
	
	public void updateRoute() {
		this.interrupter.setInterrupt(true);
	}
	
	public void setAmblulanceRoute(String vId, List<Integer> edges) {
		this.ambulanceRoutes.put(vId, edges);
		this.updateRoute();
	}

	@Override
	protected void addMetainformationWhenPublishLocation(JsonObject event) {
		// nothing to add
	}

	@Override
	protected void configureDeviceClient(DeviceClient deviceClient) {
	}
	
	private class StupidInterruptSolution implements Interruption {

		private boolean interrupt = false;

		@Override
		public boolean interrupt() {
			if(interrupt){
				//interrupt = false;
				return true;
			}
			return false;
		}


		public void setInterrupt(boolean interrupt){
			this.interrupt = interrupt;
		}
	}
}
