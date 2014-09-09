package com.jalatif.Chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientChatHandler extends Activity implements View.OnClickListener, Runnable{
    /**
     * Called when the activity is first created.
     */

    TextView tvStatus, tvDebug;
    EditText etWName, etMessage;
    String message = "Kuch Nahi";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initVars();
        Thread listen = new Thread(this, "Listen");
        listen.start();

    }

    private void initVars(){
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvDebug = (TextView) findViewById(R.id.tvDebug);
        etWName = (EditText) findViewById(R.id.etwName);
        etMessage = (EditText) findViewById(R.id.etMessage);
    }

    private void Connection(){
        final int REDIRECTED_SERVERPORT = 1234;
        String serverIpAddress = "192.168.1.3";
        try {
            InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
            Socket socket = new Socket(serverAddr, REDIRECTED_SERVERPORT);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String st = null;
            st = input.readLine();
            message = st;

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        tvDebug.setText(message);

    }

    @Override
    public void onClick(View v) {
        //To change body of implemented methods use File | Settings | File Templates.
        switch (v.getId()){

        }
    }

    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
        Connection();
    }
}
