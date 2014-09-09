package com.jalatif.Chat;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.IOException;
import java.util.Timer;

/**
 * Created with IntelliJ IDEA.
 * User: jalatif
 * Date: 4/17/13
 * Time: 10:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatFragment extends Fragment implements View.OnClickListener{
    private TextView tvStatus, tvwUser;
    private EditText etMessage;
    private Button bSend;
    private ListView lvChat;
    private String userN = "";
    private String wUser = "";
    private Context ctx;
    private ArrayAdapter<String> talk;
    private ArrayAdapter<String> lst;
    private String message = "";
    private Timer stsat = new Timer();
    private boolean visible = true;
    private boolean ouserStat = false;
    private SocketService mService;
    public static final String With_User = "WithUser";
    private String notifyMsg = "";

    /*public static final ChatFragment newInstance(String wUser)

    {
        ChatFragment f = new ChatFragment();

        Bundle bdl = new Bundle();

        bdl.putString(With_User, wUser);

        f.setArguments(bdl);

        return f;

    }       */

    public ChatFragment(){}

    public ChatFragment(String wUser, SocketService mService, String...notification){
        this.wUser = wUser;
        this.mService = mService;
        if (notification.length > 0)
            if (!notification[0].equals(""))
                this.notifyMsg = notification[0];
        System.out.println("Notification Received = " + notifyMsg);
    }

    public String getUser(){
        return wUser;
    }

    public void setSocket(SocketService sS){
        mService = sS;
    }

    public void setNotifyMsg(String notify){
        this.notifyMsg = notify;
        updateChat(this.notifyMsg);
        this.notifyMsg = "";
    }


    public void updateChat(String msg){
        if (msg.equals(""))
            return;
        talk.insert(msg, 0);
    }

    public void updateStatus(Boolean stat){
        ouserStat = stat;
        if (stat)
            tvStatus.setText("Online");
        else
            tvStatus.setText("Offline");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chatwindow, container, false);
        ctx = v.getContext();
        ouserStat = false;
        initVars(v);
        tvStatus.setText("Offline");
        //wUser = getArguments().getString(With_User);
        tvwUser.setText(wUser);
        if (talk == null)
            talk = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1);
        bSend.setOnClickListener(this);
        lvChat.setAdapter(talk);
        System.out.println("Notification Received = " + notifyMsg);
        updateChat(notifyMsg);
        notifyMsg = "";
        //setTitle("Chat B/w " + userN + " and " + wUser);
        return v;
        //return super.onCreateView(inflater, container, savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void initVars(View v){
        tvStatus = (TextView) v.findViewById(R.id.tvStatus);
        tvwUser = (TextView) v.findViewById(R.id.tvwUser);
        etMessage = (EditText) v.findViewById(R.id.etMessage);
        bSend = (Button) v.findViewById(R.id.bSend);
        lvChat = (ListView) v.findViewById(R.id.lvChat);
    }

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
            System.out.println("Ezio Problem Found in writing in Chat Fragment");
            updateStatus(false);
        }
    }
}
