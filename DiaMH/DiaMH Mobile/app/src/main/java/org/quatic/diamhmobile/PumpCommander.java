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
 * Created by utente on 13/09/2017.
 */

public class PumpCommander extends AsyncTask<String, Void, String> {
    private String message;
    private Integer dose;
    private MainActivity target;


    public PumpCommander setDose(Integer msg, Long timestamp) {
        dose=msg;
        message="{\"ins\": "+msg.toString()+", \"ts\": "+timestamp.toString()+"}";
        return this;
    }



    public PumpCommander setActivity(MainActivity t) {
        target=t;
        return this;
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            Socket socket = new Socket(params[0], Integer.parseInt(params[1]));
            socket.setSoTimeout(10000);
            PrintWriter mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            mBufferOut.println(message);
            mBufferOut.flush();
            BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String val = mBufferIn.readLine();
            socket.close();
            return val;
        } catch (IOException e) {
            Log.d("pump commander", "exception catched");
            e.printStackTrace();
            return "error";
        }

    }

    @Override
    protected void onPostExecute(String res) {
        Log.d("pump commander", "setting text");
        Log.d("pump commander", res);
        target.pumpCallback(res);
    }


}
