package com.jalatif.Chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: jalatif
 * Date: 4/14/13
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class PortInfo extends Activity implements View.OnClickListener, Runnable {
    private EditText etHost, etPort;
    private Button bSubmit;
    private TextView tvLog;
    //private String host = "192.168.1.3";
    //private int port = 1234;
    //private String host = "jalatif.no-ip.biz";
    //private String host = "jalatif.read-books.org";
    private String host = "jalatif.unusualperson.com";
    private int port = 1234;
    private Context ctx;
    //static DataOutputStream dout;
    //static DataInputStream din;
    //private String logTxt = "";

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            System.out.println("Login Got Messenger");
            String logTxt = (String) message.obj;
            System.out.println("Login log = " + logTxt + " " + message.arg1);
            if (message.arg1 == RESULT_OK && logTxt != null) {
                //Toast.makeText(PortInfo.this, logTxt, Toast.LENGTH_LONG).show();
                Intent loginIntent = new Intent(ctx, Login.class);
                startActivity(loginIntent);
                System.out.println("Started Login Activity");
            } else {
                //Toast.makeText(PortInfo.this, logTxt, Toast.LENGTH_LONG).show();
            }

        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.portinfo);
        initVars();
        ctx = this;
        bSubmit.setOnClickListener(this);

    }

    private void initVars(){
        etHost = (EditText) findViewById(R.id.etHost);
        etPort = (EditText) findViewById(R.id.etPort);
        bSubmit = (Button) findViewById(R.id.bSubmit);
        tvLog = (TextView) findViewById(R.id.tvLog);
        etHost.setText(host);
        etPort.setText(String.valueOf(port));

    }

    @Override
    public void onClick(View v) {
        //To change body of implemented methods use File | Settings | File Templates.
        try{
            host = etHost.getText().toString();
            port = Integer.parseInt(etPort.getText().toString());
            Thread tryConn = new Thread(this, "Connection");
            tryConn.start();
        }
        catch (NumberFormatException nfe){
            tvLog.setText("Port Number should be a Number");
        }


    }

    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
        //try{
        Intent socketMakingIntent = new Intent(ctx, SocketService.class);
        Messenger messenger = new Messenger(handler);
        socketMakingIntent.putExtra("MESSENGER", messenger);
        socketMakingIntent.putExtra("Host", host);
        socketMakingIntent.putExtra("Port", port);
        startService(socketMakingIntent);
            //ClientChatHandler cch = new ClientChatHandler(ip, port);
            /*socket = new Socket( host, port );
            dout = new DataOutputStream(socket.getOutputStream());
            din = new DataInputStream(socket.getInputStream());

            /*MySocket ms = new MySocket(socket);
            //Start new Activity this.setVisible(false);
            logTxt = "Connection can be made here";
            Bundle b = new Bundle();
            b.putParcelable("com.jalatif.Chat.mysocket", ms);

            Intent loginIntent = new Intent(ctx, Login.class);
            startActivity(loginIntent);
            */
            //ClientChatHandler cch = new ClientChatHandler(ip, port, socket);
        /*}
        catch(UnknownHostException uhe){
            logTxt = "Unknow host : " + host;
        }
        catch (ConnectException ce){
            logTxt = "Wrong Port Number";
        }
        catch (IOException e) {
            logTxt = "Invalid Host Name";
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch(IllegalArgumentException iae){
            logTxt = "Port out of range";
        }
        runOnUiThread(new Runnable() {
            public void run() {
            //stuff that updates ui
                tvLog.setText(logTxt);
                //logTxt = "";
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        stopService(new Intent(this, SocketService.class));
    }
}
