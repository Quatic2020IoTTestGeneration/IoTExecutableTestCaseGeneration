package DiaMHTestsMaven.wrappers;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class InsulinPump implements IPump {
	private static final String INS_DOSE_TOPIC = "diamh/client-id/ins_dose";
	private static final String MQTT_BROKER = "tcp://mqtt.eclipse.org:1883";
	private static final String COMMAND_TOPIC ="diamh/client-id/phone_command";
	private static final String PUMP_RESET = "diamh/client-id/pump_cmd/reset";
	private static final String PUMP_POWER = "diamh/client-id/pump_cmd/power";
	private int erogatedInjections;
	
	public InsulinPump() {
		erogatedInjections = 0;
	}
	
	/* (non-Javadoc)
	 * @see DiaMHTestsMaven.DiaMHTests.IPump#reset()
	 */
	@Override
	public void reset() {
	        try {
	        	String clientId = MqttClient.generateClientId();
	        	MqttConnectOptions conOpt = new MqttConnectOptions();
	        	conOpt.setCleanSession(true);
	        	MqttClient sampleClient = new MqttClient(MQTT_BROKER, clientId, new MemoryPersistence());
	            sampleClient.connect(conOpt);
	            
	            //Reset insuline
	            MqttMessage message = new MqttMessage("0".getBytes());
	            message.setQos(1);
	            sampleClient.publish(PUMP_RESET, message);
	            Thread.sleep(1000);
	            sampleClient.publish(INS_DOSE_TOPIC, message);
	            System.out.println("Message published");
	            Thread.sleep(1000);
	            sampleClient.disconnect();
	            System.out.println("Disconnected");
	            
	        } catch(Exception me) {
	            
	            System.out.println("msg "+me.getMessage());
	            System.out.println("loc "+me.getLocalizedMessage());
	            System.out.println("cause "+me.getCause());
	            System.out.println("excep "+me);
	            me.printStackTrace();
	        } finally {
	        	erogatedInjections = 0;	
	        }
	}
	
	public void power(String status) {
        try {
        	String clientId = MqttClient.generateClientId();
        	MqttConnectOptions conOpt = new MqttConnectOptions();
        	conOpt.setCleanSession(true);
        	MqttClient sampleClient = new MqttClient(MQTT_BROKER, clientId, new MemoryPersistence());
            sampleClient.connect(conOpt);
            
            //Reset insuline
            MqttMessage message = new MqttMessage(status.getBytes());
            message.setQos(1);
            sampleClient.publish(PUMP_POWER, message);
            
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
            
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        } finally {
        	erogatedInjections = 0;	
        }
	}

	@Override
	public void inject() {
		erogatedInjections++;
		
	}

	@Override
	public int getErogatedInjections() {
		// TODO Auto-generated method stub
		return erogatedInjections;
	}
}
