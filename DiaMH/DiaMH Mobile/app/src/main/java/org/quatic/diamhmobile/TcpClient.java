package org.quatic.diamhmobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by utente on 13/09/2017.
 */

public class TcpClient extends Thread {

    private String HOST; //your computer IP address
    private int PORT;
    private Activity main;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived messageCallback;
    private Runnable connectionCallback;
    private Runnable errorCallback;

    // while this is true, the server will continue running
    private boolean mRun = false;
    private Socket socket;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    private volatile String outMsg;
    private Handler handler;



    public TcpClient(Activity act, String host, Integer port) {
        main = act;
        HOST = host;
        PORT = port;

    }
    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient setMessageCallback(OnMessageReceived listener) {
        messageCallback = listener;
        return this;
    }

    public TcpClient setConnectionCallback(Runnable r) {
        connectionCallback=r;
        return this;
    }

    public TcpClient setErrorCallback(Runnable r) {
        errorCallback=r;
        return this;
    }





    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        Log.d("Debug", "stopClient");

        // send mesage that we are closing the connection
        //sendMessage(Constants.CLOSED_CONNECTION + "Kazy");

        mRun = false;

       /* if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }*/

        messageCallback = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.


            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(HOST, PORT);
            socket.setKeepAlive(true);
            if(socket.isConnected()) {
                main.runOnUiThread(connectionCallback);
            } else {
                main.runOnUiThread(errorCallback);
            }
            try {
                Log.d("Debug", "inside try catch");
                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                // send login name
                //sendMessage(Constants.LOGIN_NAME + PreferencesManager.getInstance().getUserName());
                //sendMessage("Hi");
                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();
                    if (mServerMessage != null && messageCallback != null) {
                        //call the method messageReceived from MyActivity class
                        messageCallback.setMessage(mServerMessage);
                        main.runOnUiThread(messageCallback);
                        //notifyMessage(mServerMessage);
                    }

                }
                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                Log.d("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            main.runOnUiThread(errorCallback);
            Log.d("TCP", "C: Error", e);

        }

    }

    private void notifyMessage(String str) {
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("refresh", ""+str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    public Socket getSocket() {
        return socket;
    }


    public abstract static class OnMessageReceived implements Runnable {

        protected String message;


        void setMessage(String message) {
            this.message = message;
        }

    }
}
