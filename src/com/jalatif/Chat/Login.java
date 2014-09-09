package com.jalatif.Chat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jalatif
 * Date: 4/14/13
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class Login extends Activity implements View.OnClickListener, Runnable{
    private EditText etUsername, etPassword;
    private Button bLogin, bRegister;
    private ToggleButton tbShow;
    private TextView tvLog;
    private String username, password, tvl;
    private String userN = "";
    private String userP = "";
    private String auth = "";
    private Context ctx;
    //private Socket socket;
    //private DataOutputStream dout;
    //private DataInputStream din;
    boolean mBound = false;
    private SocketService mService;
    Thread authorizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.login);
        initVars();
        ctx = getApplicationContext();
        authorizer = new Thread(this, "AuthCheck");
        //Bundle extras = getIntent().getExtras();
        //MySocket ms = extras.getParcelable("com.jalatif.Chat.mysocket");
        //Jalatif socket = PortInfo.socket;
        //tvLog.setText("Chal Raha hai");
        //tvLog.append(String.valueOf(socket.getLocalPort()));
        bLogin.setOnClickListener(this);
        bRegister.setOnClickListener(this);
        tbShow.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();    //To change body of overridden methods use File | Settings | File Templates.
        Intent mIntent = new Intent(this, SocketService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
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
            mBound = true;
            System.out.println("Service Connected to Login");
            authorizer.start();
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

    private void initVars(){
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        bRegister = (Button) findViewById(R.id.bRegister);
        tbShow = (ToggleButton) findViewById(R.id.tbShow);
        tvLog = (TextView) findViewById(R.id.tvLoginLog);
    }

    @Override
    public void onClick(View v) {
        //To change body of implemented methods use File | Settings | File Templates.
        //Thread auth = new Thread(this, "AuthCheck");
        switch(v.getId()){
            case R.id.tbShow:
                if (tbShow.isChecked()){
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                break;

            case R.id.bLogin:
                tvLog.setText("Waiting for Authentication");
                username = etUsername.getText().toString();
                userN = username;
                password = etPassword.getText().toString();
                userP = password;
                try {
                    //dout.writeUTF(username + "AuthU@*@~" + password);
                    mService.writeMessage(username + "AuthU@*@~" + password);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                //auth.start();
                break;

            case R.id.bRegister:
                username = etUsername.getText().toString();
                password = etPassword.getText().toString();
                if (username.length() > 20 || password.length() > 20 || username.contains("&") || username.equals("") || password.equals("")){
                    tvLog.setText("Register with valid information");
                }
                try {
                    //dout.writeUTF( username + "RegsU@*@~" + password );
                    mService.writeMessage( username + "RegsU@*@~" + password );
                    //auth.start();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                break;
        }
    }


    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
            while (true){
                try {
                    auth = mService.readMessage();//din.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    continue;
                }
                System.out.println(auth);
                if (auth.contains("Heart~*~@")){
                    try {
                        mService.writeMessage("Heart@*@~");
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    continue;
                }
                if (auth.contains("RegsR~*~@")){
                    auth = auth.substring(9, auth.length());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //To change body of implemented methods use File | Settings | File Templates.
                            tvLog.setText(auth);
                            etUsername.setText("");
                            etPassword.setText("");
                        }
                    });
                    continue;
                }
                if (!auth.contains("AuthR~*~@"))
                    continue;
                auth = auth.substring(9, auth.length());
                if (auth.equals("true"))
                    tvl = "Successful Login";
                else
                    tvl = "Invalid Username or Password";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //To change body of implemented methods use File | Settings | File Templates.
                        tvLog.setText(tvl);
                    }
                });
                if (tvl.equals("Successful Login"))
                    break;
            }
        mService.setDetails(userN, userP);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Intent avUser = new Intent(ctx, AvailableUsers.class);
        avUser.putExtra("username", userN);
        startActivity(avUser);

    userN = etUsername.getText().toString();
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        if (mBound){
            unbindService(mConnection);
            mBound = false;
        }
        //finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        if (mBound){
            unbindService(mConnection);
            mBound = false;
        }
        //stopService(new Intent(this, SocketService.class));
    }
}
