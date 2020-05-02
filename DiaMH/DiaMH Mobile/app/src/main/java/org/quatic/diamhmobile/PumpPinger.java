package org.quatic.diamhmobile;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by utente on 22/09/2017.
 */

public class PumpPinger extends AsyncTask<Void, Void, String> {

    private MainActivity target;
    private String host;
    private int port;

    public PumpPinger(MainActivity target, String host, int port) {
        this.target = target;
        this.host = host;
        this.port = port;
    }

    @Override
    public String doInBackground(Void... params) {
        try {
            Log.d("pump pinger", "pinging");
            Socket socket = new Socket(host, port);
            socket.setSoTimeout(5000);
            PrintWriter mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            mBufferOut.println("{\"ping\" : 1}");
            mBufferOut.flush();
            BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String res = mBufferIn.readLine();
            socket.close();
            return res;


        } catch (IOException e) {
            Log.d("pump pinger", "exception catched");
            e.printStackTrace();
            return "error";
        }
    }

    protected void onPostExecute(String res) {
        if(!res.equals("{\"pong\" : 1}")) {
            target.pumpCallback("error");
        } else {
            target.pumpCallback("ok");
        }
    }
}
