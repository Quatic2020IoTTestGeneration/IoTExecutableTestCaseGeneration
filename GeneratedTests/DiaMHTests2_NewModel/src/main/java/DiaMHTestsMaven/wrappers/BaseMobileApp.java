package DiaMHTestsMaven.wrappers;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public abstract class BaseMobileApp implements MobileApp {
	
	private static final String MQTT_BROKER = "tcp://mqtt.eclipse.org:1883";
	private static final String COMMAND_TOPIC ="diamh/client-id/phone_command";
	
	@Override
	public void shutdownAlarm() {
		try {
        	String clientId = MqttClient.generateClientId();
        	MqttClient sampleClient = new MqttClient(MQTT_BROKER, clientId, new MemoryPersistence());
            System.out.println("Connecting to broker: "+MQTT_BROKER);
            sampleClient.connect();
            System.out.println("Connected");
            
            //Reset alarm
            MqttMessage message = new MqttMessage("{\"alarm\" : 0}".getBytes());
            message.setQos(1);
            sampleClient.publish(COMMAND_TOPIC, message);
            
            
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
        }
	}

}
