package DiaMHTestsMaven.wrappers;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ThreadLocalRandom;

public class GlucoseSensor implements IGlucoseSensor {
	
	private static final String MQTT_BROKER = "tcp://mqtt.eclipse.org:1883";
	private static final String START_TOPIC ="diamh/sensor/start";
	private static final String COMMAND_TOPIC ="diamh/client-id/phone_command";
	private static final String TEST_TOPIC ="diamh/sensor/test";
	private static final String GLUCOSE_LV_TOPIC = "diamh/client-id/glucose_lv";

	

	
	
	/* (non-Javadoc)
	 * @see IGlucoseSensor#sendMessage(java.lang.String)
	 */
	@Override
	public void sendMessage(String pattern) {
        try {
        	String clientId = MqttClient.generateClientId();
        	MqttConnectOptions conOpt = new MqttConnectOptions();
        	conOpt.setCleanSession(true);
        	MqttClient sampleClient = new MqttClient(MQTT_BROKER, clientId, new MemoryPersistence());
            sampleClient.connect(conOpt);
            
            MqttMessage message = new MqttMessage(pattern.getBytes());
            message.setQos(1);
            sampleClient.publish(START_TOPIC, message);
            sampleClient.disconnect();
            
            
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
	}
	
	
	public void sendAndWait(int total, int over, int treshold) {
		Subscriber listener = null;
		try {
			listener = new Subscriber(MQTT_BROKER, GLUCOSE_LV_TOPIC);
			
			
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String msgStr = "{\"total\" : "+total+", \"over\" : "+over+", \"treshold\" : "+treshold+" }";
		sendMessage(msgStr);
		int prev = 0;
		while(listener.receivedMessages() < total) {
			///NON RIMUOVERE, se nel while di attesa non si fa niente la funziona si bloccherà
			try {
				Thread.sleep(500);
				if(listener.receivedMessages() > prev) {
					prev++;
					System.out.println("Received "+prev+" of "+total);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			listener.unsubscribe();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendOver(int treshold) {
		sendMessage("{\"treshold\" : "+treshold+", \"kind\" : \"over\"}");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendUnder(int treshold) {
		sendMessage("{\"treshold\" :"+treshold+", \"kind\" : \"under\"}");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see IGlucoseSensor#stop()
	 */
	@Override
	public void stop() {
        try {
        	String clientId = MqttClient.generateClientId();
        	MqttConnectOptions conOpt = new MqttConnectOptions();
        	conOpt.setCleanSession(true);
        	MqttClient sampleClient = new MqttClient(MQTT_BROKER, clientId, new MemoryPersistence());
            sampleClient.connect(conOpt);
            
            MqttMessage message = new MqttMessage("{\"ctrl\": \"false\"}".getBytes());
            message.setQos(1);
            sampleClient.publish(START_TOPIC, message);
            
            sampleClient.disconnect();
            
            
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
	}
	
	public void setFilter(Integer n) {
        try {
        	String clientId = MqttClient.generateClientId();
        	MqttConnectOptions conOpt = new MqttConnectOptions();
        	conOpt.setCleanSession(true);
        	MqttClient sampleClient = new MqttClient(MQTT_BROKER, clientId, new MemoryPersistence());
            sampleClient.connect(conOpt);
            
            MqttMessage message = new MqttMessage(("{\"filter\": "+n+"}").getBytes());
            message.setQos(1);
            sampleClient.publish(TEST_TOPIC, message);
            
            sampleClient.disconnect();
            
            
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
	}
}
