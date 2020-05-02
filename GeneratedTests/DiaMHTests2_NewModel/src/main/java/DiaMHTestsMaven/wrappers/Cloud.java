package DiaMHTestsMaven.wrappers;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Cloud implements ICloud {
	private static final String CLOUD_RESET = "diamh/cloud/reset";
	private static final String MQTT_BROKER = "tcp://mqtt.eclipse.org:1883";
	
	private int valuesToDiscard;
	private int threshold;
	private int numReadings;
	private int maxReadings;
	private int[] glucoseReadings;
	
	
	
	public Cloud() {
        valuesToDiscard = 0;
        threshold = 160;
        numReadings = 0;
        maxReadings = 20;
        glucoseReadings = new int[maxReadings];
	}
	
	@Override
	public void reset() {
		try {
			String clientId = MqttClient.generateClientId();

	        System.out.println("Connecting to broker: "+MQTT_BROKER);
        	MqttConnectOptions conOpt = new MqttConnectOptions();
        	conOpt.setCleanSession(true);
            MqttClient sampleClient = new MqttClient(MQTT_BROKER, clientId, new MemoryPersistence());
            sampleClient.connect(conOpt);
	        System.out.println("Connected");
	        
			MqttMessage message = new MqttMessage("1".getBytes());
	        message.setQos(1);
	        sampleClient.publish(CLOUD_RESET, message);
		}catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        } finally {
            valuesToDiscard = 0;
            threshold = 160;
            numReadings = 0;
            maxReadings = 20;
            glucoseReadings = new int[maxReadings];
        }
	}


	@Override
	public int getCriticalCount() {
		int count = 0;
		for(int i: glucoseReadings) {
			if(i>threshold) count++;
		}
		return count;
	}


	@Override
	public void receiveUnder() {
		if(valuesToDiscard == 0) {
			glucoseReadings[numReadings] = threshold-1;
            numReadings++;
		}
        else {
            valuesToDiscard--;
        }
	}


	@Override
	public void receiveOver() {
		if(valuesToDiscard == 0) {
			glucoseReadings[numReadings] = threshold+1;
            numReadings++;
		}
        else {
            valuesToDiscard--;
        }
		
	}


	@Override
	public void resetReadings() {
		numReadings = 0;
		glucoseReadings = new int[maxReadings];
	}


	@Override
	public void discardNext(int qty) {
		valuesToDiscard = qty;		
	}


	@Override
	public int getNumReadings() {
		return numReadings;
	}


	@Override
	public int getMaxReadings() {
		return maxReadings;
	}


	@Override
	public int getThreshold() {
		return threshold;
	}
	
	
	
	
	
	
}
