package com.capgemini.hackathon.device.service;

import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.Charset;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.util.LoggerUtility;

public class DeviceClientCustom extends DeviceClient{
	
	/**
	 * Overrided version of DeviceClient to change the JSON Messages
	 * to format Geospatial needs.
	 */
	
private static final String CLASS_NAME = DeviceClientCustom.class.getName();
	
	private static final Pattern COMMAND_PATTERN = Pattern.compile("iot-2/cmd/(.+)/fmt/(.+)");
	
	private CommandCallback commandCallback = null;

	private APIClient apiClient;

	public DeviceClientCustom(MqttAsyncClient arg0) {
		super(arg0);
	
	}

	public DeviceClientCustom(MqttClient arg0) {
		super(arg0);
		
	}

	public DeviceClientCustom(Properties options) throws Exception {
		super(options);
		LoggerUtility.fine(CLASS_NAME, "DeviceClientCustom", "options   = " + options);
		this.clientId = "d" + CLIENT_ID_DELIMITER + getOrgId() + CLIENT_ID_DELIMITER + getDeviceType() + CLIENT_ID_DELIMITER + getDeviceId();
		
		if (getAuthMethod() == null) {
			this.clientUsername = null;
			this.clientPassword = null;
		}
		else if (!getAuthMethod().equals("token")) {
			throw new Exception("Unsupported Authentication Method: " + getAuthMethod());
		}
		else {
			// use-token-auth is the only authentication method currently supported
			this.clientUsername = "use-token-auth";
			this.clientPassword = getAuthToken();
		}
		createClient(this.new MqttDeviceCallBack());
		
		options.setProperty("auth-method", "device");
		
		apiClient = new APIClient(options);
	}
	
	public String getDeviceType() {
		String type;
		type = options.getProperty("type");
		if(type == null) {
			type = options.getProperty("Device-Type");
		}
		return trimedValue(type);
	}

	public String getFormat() {
		String format = options.getProperty("format");
		if(format != null && ! format.equals(""))
			return format;
		else
			return "json";
		
	}

	
	private void subscribeToCommands() {
		try {
			mqttAsyncClient.subscribe("iot-2/cmd/+/fmt/+", 2);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public APIClient api() {
		return this.apiClient;
	}

	public boolean publishEvent(String event, Object data) {
		return publishEvent(event, data, 0);
	}

	
	
	@Override
	public boolean publishEvent(String event, Object data, int qos) {
		if (!isConnected()) {
			return false;
		}
		final String METHOD = "publishEvent(2)";
		JsonObject payload = new JsonObject();
		
		String timestamp = ISO8601_DATE_FORMAT.format(new Date());
		payload.addProperty("ts", timestamp);
		
		// Handle null object
		if(data == null) {
			data = new JsonObject();
		}
		
		JsonElement dataElement = gson.toJsonTree(data);
		payload.add("d", dataElement);
		
		String topic = "iot-2/evt/" + event + "/fmt/json";
		
		JsonObject dataJson = (JsonObject) data;
		payload.add("latitude", dataJson.get("latitude"));
		payload.add("longitude", dataJson.get("longitude"));
		payload.add("vin", dataJson.get("vin"));

		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			mqttAsyncClient.publish(topic, msg).waitForCompletion();
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

		private class MqttDeviceCallBack implements MqttCallbackExtended {
			
			/**
			 * If we lose connection trigger the connect logic to attempt to
			 * reconnect to the IBM Watson IoT Platform.
			 * 
			 * @param exception
			 *            Throwable which caused the connection to get lost
			 */
			public void connectionLost(Throwable exception) {
				final String METHOD = "connectionLost";
				LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, exception.getMessage());
				try {
					reconnect();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			/**
			 * A completed deliver does not guarantee that the message is received by the service
			 * because devices send messages with Quality of Service (QoS) 0. <br>
			 * 
			 * The message count
			 * represents the number of messages that were sent by the device without an error on
			 * from the perspective of the device.
			 * @param token
			 *            MQTT delivery token
			 */
			public void deliveryComplete(IMqttDeliveryToken token) {
				final String METHOD = "deliveryComplete";
				LoggerUtility.fine(CLASS_NAME, METHOD, "token " + token.getMessageId());
				messageCount++;
			}
			
			/**
			 * The Device client does not currently support subscriptions.
			 */
			public void messageArrived(String topic, MqttMessage msg) throws Exception {
				final String METHOD = "messageArrived";
				if (commandCallback != null) {
					/* Only check whether the message is a command if a callback 
					 * has been defined, otherwise it is a waste of time
					 * as without a callback there is nothing to process the generated
					 * command.
					 */
					Matcher matcher = COMMAND_PATTERN.matcher(topic);
					if (matcher.matches()) {
						String command = matcher.group(1);
						String format = matcher.group(2);
						Command cmd = new Command(command, format, msg);
						LoggerUtility.fine(CLASS_NAME, METHOD, "Event received: " + cmd.toString());
						commandCallback.processCommand(cmd);
				    }
				}
			}

			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				// TODO Auto-generated method stub
				
			}

		}
		
		public void setCommandCallback(CommandCallback callback) {
			this.commandCallback  = callback;
		}
	}
