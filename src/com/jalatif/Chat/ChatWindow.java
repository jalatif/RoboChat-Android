package com.jalatif.Chat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.*;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: jalatif
 * Date: 4/14/13
 * Time: 11:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatWindow extends Activity implements View.OnClickListener, Runnable{
    private TextView tvStatus, tvwUser;
    private EditText etMessage;
    private Button bSend;
    private ListView lvChat;
    private String userN = "";
    private String wUser = "";
    private Context ctx;
    private ArrayAdapter<String> talk;
    private String message = "";
    private Timer stsat = new Timer();
    //private Socket socket;
    //private DataOutputStream dout;
    //private DataInputStream din;
    boolean mBound = false;
    private SocketService mService;
    private boolean visible = true;
    private boolean ouserStat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.chatwindow);
        ctx = this;
        visible = true;
        ouserStat = true;
        talk = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1);
        Bundle b = getIntent().getExtras();
        userN = b.getString("UserName");
        wUser = b.getString("toUser");
        setTitle("Chat B/w " + userN + " and " + wUser);
        initVars();
        tvwUser.setText(wUser);
        bSend.setOnClickListener(this);
        lvChat.setAdapter(talk);
        Intent mIntent = new Intent(this, SocketService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        int seconds = 1;
        stsat.schedule(new statusCheck(), 0, seconds * 1000);
        Thread cht = new Thread(this, "Chat");
        cht.start();
    }

    private void initVars(){
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvwUser = (TextView) findViewById(R.id.tvwUser);
        etMessage = (EditText) findViewById(R.id.etMessage);
        bSend = (Button) findViewById(R.id.bSend);
        lvChat = (ListView) findViewById(R.id.lvChat);
    }

    @Override
    protected void onStart() {
        super.onStart();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        visible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SocketService.SocketBinder binder = (SocketService.SocketBinder) service;
            mService = binder.getService();
            try {
                mService.writeMessage(userN + "SendTo@*@~" + "");
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            mBound = true;
            System.out.println("Service Connected to Login");
            //dout = mService.getDout();//new SocketService().getDout();
            //din = mService.getDin();//new SocketService().getDin();
            //socket = mService.getSocket();//new SocketService().getSocket();
            //System.out.println("Jalatif Socket is " + socket);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onClick(View v) {
        //To change body of implemented methods use File | Settings | File Templates.
        try {
            message = etMessage.getText().toString();
            if(mService != null){
                if (ouserStat){
                    //dout.writeUTF(wUser + "SendTo@*@~" + message);
                    mService.writeMessage(wUser + "SendTo@*@~" + message);
                    etMessage.setText("");
                    talk.insert("I said : " + message, 0);
                    //talk.add("I said : " + message);
                    }
                else{
                    mService.writeMessage(wUser + "OfMsg@*@~" + message);
                    etMessage.setText("");
                    talk.insert("I said : " + message + " \n(" + wUser + " is offline for now.\n He'll receive ur message when he comes online.)\n", 0);
                    //dout.writeUTF(to + "OfMsg@*@~"+message);
                }
            }
            else{
                 talk.insert("Connection Problem.. Wait", 0);
                //talk.add("Connection Problem.. Wait");
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }



    private class statusCheck extends TimerTask {
        public void run(){
            try {
                if (mService != null && visible){
                    mService.writeMessage("StsAt@*@~" + wUser);
                    mService.writeMessage("Status@*@~");
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
        try {
            while (visible) {
                //dout.writeUTF("Status@*@~");
                if (mService == null)
                    continue;
                System.out.println("Working here");
                String msp = mService.readMessage();//din.readUTF();
                String message = "";
                System.out.println("De Villiers got message : " + msp);
                if (msp.contains("MsgRx~*~@")){
                    int loc = msp.indexOf("MsgRx~*~@");
                    String from = msp.substring(0, loc);
                    message = msp.substring(loc + 9, msp.length());
                    System.out.println("De Villiers got message : " + message);
                    final String uiMsg = message;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //To change body of implemented methods use File | Settings | File Templates.
                            if (!uiMsg.equals(""))
                                talk.insert(uiMsg, 0);
                            //talk.add(uiMsg);
                        }
                    });
                }
                if(msp.startsWith("StsAt~*~@")){
                    final String status = msp.substring(9, msp.length());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //To change body of implemented methods use File | Settings | File Templates.
                            if (status.equals("true")){
                                tvStatus.setText("Online");
                                ouserStat = true;
                            }
                            else{
                                tvStatus.setText("Offline");
                                ouserStat = false;
                            }
                        }
                    });
                }
                if (msp.startsWith("StsOf~*~@")){
                    message = msp.substring(9, msp.length());
                    String membs[] = message.split("&");
                    //this.setUsers(membs);

                }
            }
        }
        catch( IOException ie ) { System.out.println( ie ); }

    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        stsat.cancel();
        visible = false;
        unbindService(mConnection);
        //finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        unbindService(mConnection);
        stopService(new Intent(this, SocketService.class));
    }

}
