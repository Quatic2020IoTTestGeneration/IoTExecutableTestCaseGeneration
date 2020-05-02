package DiaMHTestsMaven.wrappers;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 */
public class Subscriber implements MqttCallback {

    private final int qos = 1;
    private String topic;
    private MqttClient client;
    private int received;



    public Subscriber(String broker, String topic) throws MqttException {
    	this.topic = topic;
    	this.received = 0;
        String clientId = MqttClient.generateClientId();
    	MqttConnectOptions conOpt = new MqttConnectOptions();
    	conOpt.setCleanSession(true);
    	this.client = new MqttClient(broker, clientId, new MemoryPersistence());

        this.client.setCallback(this);
        this.client.connect(conOpt);
        this.client.subscribe(this.topic, qos);
    }

    private String[] getAuth(URI uri) {
        String a = uri.getAuthority();
        String[] first = a.split("@");
        return first[0].split(":");
    }

    public void sendMessage(String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        this.client.publish(this.topic, message); // Blocking publish
    }

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost because: " + cause);
        System.exit(1);
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
    
    public int receivedMessages() {
    	return received;
    }
    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
        System.out.println(String.format("[%s] %s", topic, new String(message.getPayload())));
        try {
        	Integer.parseInt(new String(message.getPayload()));
        	received += 1;
        } catch (NumberFormatException e) {
        	System.out.println(new String(message.getPayload()));
        }
    }
    
    public void unsubscribe() throws MqttException {
    	client.unsubscribe(topic);
    }
    
   


}