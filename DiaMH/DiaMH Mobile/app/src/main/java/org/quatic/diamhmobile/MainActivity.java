package org.quatic.diamhmobile;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;


import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static String MQTT_BROKER;
    public static String CMD_TOPIC;
    public static String SENSOR_TOPIC;
    public static String GLUCOSE_TOPIC;
    public static String INSULINE_TOPIC;
    public static String PUMP_CMD_TOPIC;
    public static String HOST;

    MqttAndroidClient client;
    TextView glcLevel;
    TextView insLevel;
    TextView sensorStat;
    TextView cloudStat;
    TextView pumpStat;
    ImageView alertIcon;
    TextView alarmTxt;
    Button doctorBtn;
    Button insConfBtn;
    Button insCancBtn;
    TextView insConfText;
    Integer pendingInsulin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConsts();
        setupLayout();
        pendingInsulin=0;
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTT_BROKER,
                clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this, "Connected to DiaMH Cloud", Toast.LENGTH_LONG).show();
                    subscribeCmd();
                    subscribeSensor();
                    subscribeInsulin();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Connection to DiaMH Cloud failed", Toast.LENGTH_LONG).show();
                    glcLevel.setText("MQTT connection failed");

                }
            });
        } catch (MqttException e) {
            glcLevel.setText("MQTT exception");
            e.printStackTrace();
        }


        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

                glcLevel.setText("-?-");
                insLevel.setText("-?-");
                sensorStat.setText("FAIL");
                pumpStat.setText("FAIL");
                cloudStat.setText("FAIL");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.equals(CMD_TOPIC)) {
                    JSONObject cmd = new JSONObject(message.toString());
                    if(cmd.isNull("alarm")) {
                        Long ts = cmd.optLong("timestamp");
                        Log.d("insts", ts.toString());
                        showInsulineConf(cmd.getInt("insuline"), cmd.getLong("timestamp"));
                    } else {
                        if(cmd.getInt("alarm") == 1) {
                            Toast.makeText(MainActivity.this, "!!! ALARM !!!", Toast.LENGTH_LONG).show();
                            alarmTxt.setVisibility(View.VISIBLE);
                            alertIcon.setVisibility(View.VISIBLE);
                            doctorBtn.setVisibility(View.VISIBLE);
                        } else if(cmd.getInt("alarm") == 0) {
                            alarmTxt.setVisibility(View.GONE);
                            alertIcon.setVisibility(View.GONE);
                            doctorBtn.setVisibility(View.GONE);
                        }

                    }
                } else if(topic.equals(INSULINE_TOPIC)){
                    insLevel.setText(message.toString());
                    insConfText.setVisibility(View.GONE);
                    insConfBtn.setVisibility(View.GONE);
                    insCancBtn.setVisibility(View.GONE);
                }
                else {
                    glcLevel.setText("Message received with unrecognized topic");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        new PumpPinger(this, HOST, 2692).execute();
        insLevel.setText("0");
        glcLevel.setText("-?-");
    }

    private void subscribeInsulin() {

        try {

            IMqttToken subToken = client.subscribe(INSULINE_TOPIC, 1);

            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(MainActivity.this, "Subscription to pump failed", Toast.LENGTH_LONG).show();
                    pumpStat.setText("FAIL");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    private void initConsts() {
        MQTT_BROKER = getString(org.quatic.diamhmobile.R.string.mqtt_broker);
        CMD_TOPIC = getString(org.quatic.diamhmobile.R.string.topic_cmd);
        SENSOR_TOPIC = getString(org.quatic.diamhmobile.R.string.topic_sensor);
        GLUCOSE_TOPIC = getString(org.quatic.diamhmobile.R.string.topic_glucose);
        INSULINE_TOPIC = getString(org.quatic.diamhmobile.R.string.topic_ins);
        PUMP_CMD_TOPIC = getString(org.quatic.diamhmobile.R.string.topic_pump_cmd);
        HOST = getString(org.quatic.diamhmobile.R.string.host);
    }

    private void setupLayout() {
        setContentView(org.quatic.diamhmobile.R.layout.activity_main);
        glcLevel = (TextView)findViewById(org.quatic.diamhmobile.R.id.glcLevel);
        insLevel = (TextView)findViewById(org.quatic.diamhmobile.R.id.insLevel);
        sensorStat = (TextView)findViewById(org.quatic.diamhmobile.R.id.sensorStat);
        cloudStat = (TextView)findViewById(org.quatic.diamhmobile.R.id.cloudStat);
        pumpStat = (TextView)findViewById(org.quatic.diamhmobile.R.id.pumpStat);
        alertIcon = (ImageView)findViewById(org.quatic.diamhmobile.R.id.alertIcon);
        alarmTxt = (TextView)findViewById(org.quatic.diamhmobile.R.id.alarmTxt);
        doctorBtn = (Button)findViewById(org.quatic.diamhmobile.R.id.doctorBtn);
        insConfBtn = (Button)findViewById(org.quatic.diamhmobile.R.id.insConfBtn);
        insCancBtn = (Button)findViewById(org.quatic.diamhmobile.R.id.insCancBtn);
        insConfText = (TextView)findViewById(org.quatic.diamhmobile.R.id.insConfText);
        alarmTxt.setVisibility(View.GONE);
        alertIcon.setVisibility(View.GONE);
        doctorBtn.setVisibility(View.GONE);
        insConfBtn.setVisibility(View.GONE);
        insConfText.setVisibility(View.GONE);
        insCancBtn.setVisibility(View.GONE);

    }

    private void subscribeSensor() {
        Executors.newSingleThreadExecutor()
                .submit(new TcpClient(this, HOST, 2691)
                        .setMessageCallback(new TcpClient.OnMessageReceived() {
                            @Override
                            public void run() {
                                glcLevel.setText(message);
                                sendGlucose(message.getBytes());
                            }
                        })
                        .setConnectionCallback(new Runnable() {
                            @Override
                            public void run() {
                                sensorStat.setTextColor(Color.BLACK);
                                sensorStat.setText("OK");
                            }
                        })
                        .setErrorCallback(new Runnable() {
                            @Override
                            public void run() {
                                sensorStat.setTextColor(Color.RED);
                                sensorStat.setText("FAIL");
                            }
                        }));

    }

    private void subscribeCmd() {

        try {

            IMqttToken subToken = client.subscribe(CMD_TOPIC, 1);

            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    cloudStat.setTextColor(Color.BLACK);
                    cloudStat.setText("OK");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(MainActivity.this, "Subscription to command channel failed", Toast.LENGTH_LONG).show();
                    cloudStat.setTextColor(Color.RED);
                    cloudStat.setText("FAIL");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void sendGlucose(byte[] payload) {
        try {
            MqttMessage message = new MqttMessage(payload);
            client.publish(GLUCOSE_TOPIC, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void showInsulineConf(final Integer payload, final Long timestamp) {
        insConfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInsuline(payload, timestamp);
            }
        });
        insCancBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingInsulin=0;
                insConfBtn.setVisibility(View.GONE);
                insConfText.setVisibility(View.GONE);
                insCancBtn.setVisibility(View.GONE);
            }
        });
        insConfText.setText(payload.toString()+" units required");
        insConfBtn.setVisibility(View.VISIBLE);
        insConfText.setVisibility(View.VISIBLE);
        insCancBtn.setVisibility(View.VISIBLE);
    }

    public void sendInsuline(Integer message, Long timestamp) {
        Log.d("sendInsuline", "Sending "+message);
        Log.d("main activity", "getting insuline feedback");
        Toast.makeText(MainActivity.this, "Commanding insulin dose", Toast.LENGTH_LONG).show();
        new PumpCommander().setDose(message, timestamp).setActivity(this).execute(HOST, "2692");

    }

    public void pumpCallback(String feedback) {
        Log.d("pump callback", "running");
        Log.d("pump callback", feedback);
        if(feedback.equals("error")) {
            Log.d("pump callback", "error detected");
            pumpStat.setTextColor(Color.RED);
            pumpStat.setText("FAIL");
        } else if(feedback.equals("ok")) {
            pumpStat.setText("OK");
        } else {
            insLevel.setText(feedback); insConfBtn.setVisibility(View.GONE);
            insConfText.setVisibility(View.GONE);
            insCancBtn.setVisibility(View.GONE);
            pumpStat.setTextColor(Color.BLACK);
            pumpStat.setText("OK");

        }
    }
}
