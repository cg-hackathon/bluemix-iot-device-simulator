package com.capgemini.hackathon.device.service;

import org.eclipse.paho.client.mqttv3.MqttCallback;

public interface MqttCallbackExtended extends MqttCallback{
	/**
	 * Overrided version of MqttCallbackExtended. Not included in the IBM API.
	 * Required for DeviceClientCustom.
	 */
	public void connectComplete(boolean reconnect, java.lang.String serverURI);
}
