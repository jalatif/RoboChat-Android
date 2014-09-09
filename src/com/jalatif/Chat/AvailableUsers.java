package com.jalatif.Chat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jalatif
 * Date: 4/14/13
 * Time: 6:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class AvailableUsers extends Activity implements ListView.OnItemClickListener, Runnable {
    private ListView lvOnline;
    private ListView lvOffline;
    private Context ctx;
    protected Hashtable getChatWindow = new Hashtable();
    private Set<String> buddy;
    private String toUser = "";
    private String userN = "";
    private Thread stat;
    private Timer timer;// = new Timer();
    //private Socket socket;
    //private DataOutputStream dout;
    //private DataInputStream din;
    boolean mBound = false;
    private SocketService mService;
    private boolean visible = true;
    protected static int notifyId = 0;
    private String msg_uuid = "bd320120-acc2-11e2-9e96-0800200c9a66";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.availableusers);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        userN = getIntent().getExtras().getString("username");
        setTitle(userN + "'s Available Users");
        initVars();
        ctx = this;
        visible = true;
        //final String ap[] = {"Asd", "Sad", "ewr", "ewrwer"};
        //final String ap2[] = {"Abhinav", "Abhishek", "Digvijay", "Anoop", "Anshuman", "Naman", "Naveen", "Rahul"};
        //lvOnline.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ap));
        //lvOffline.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ap2));
        lvOnline.setOnItemClickListener(this);
        lvOffline.setOnItemClickListener(this);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //Intent mIntent = new Intent(this, SocketService.class);
        //bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        /*int seconds = 1;
        timer.schedule(new statusCheck(), 0, seconds*1000);

        //stat = new Thread(this, "Status");
        stat = new Thread(this, "Status");
        stat.start();
        */
    }

    @Override
    protected void onStart() {
        super.onStart();    //To change body of overridden methods use File | Settings | File Templates.
        visible = true;
        Intent mIntent = new Intent(this, SocketService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        int seconds = 2;
        timer = new Timer();
        timer.schedule(new statusCheck(), 0, seconds*1000);

        //stat = new Thread(this, "Status");
        stat = new Thread(this, "Status");
        stat.start();

    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
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
            System.out.println("Service Connected to AvailableUsers");
            try {
                mService.writeMessage("GofM@*@~");
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
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

    private void generateNotification(String from, String msg){
        // Prepare intent which is triggered if the
        // notification is selected
        if (msg.equals(""))
            return;
        /*String msg = "";
        if (msp.length > 0){
            for (String m : msp){
                msg += m + msg_uuid;
            }
        }*/
        Intent chatWindow = new Intent(ctx, TabbedChat.class);
        chatWindow.putExtra("toUser", from);
        chatWindow.putExtra("UserName", userN);
        chatWindow.putExtra("notification", msg);
        //chatWindow.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        chatWindow.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        //PendingIntent pIntent = PendingIntent.getActivity(ctx, notifyId, chatWindow, PendingIntent.FLAG_CANCEL_CURRENT);PendingIntent.FLAG_ONE_SHOT|
        PendingIntent pIntent = PendingIntent.getActivity(ctx, notifyId, chatWindow, PendingIntent.FLAG_UPDATE_CURRENT);
        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(ctx)
                .setContentTitle("New msg from " + from)
                .setContentText(msg)
                .setSmallIcon(R.drawable.talk_ldpi)
                .setContentIntent(pIntent)
                .setStyle(new Notification.BigTextStyle().bigText(msg)).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        mService.playSound();
        notificationManager.notify(notifyId++, noti);
    }

    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
        while(visible){
                if (mService == null)
                    continue;
            String msp = null;//din.readUTF();
            try {
                msp = mService.readMessage();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                continue;
            }
            String message = "";
                if (msp.contains("Heart~*~@")){
                    try {
                        mService.writeMessage("Heart@*@~");
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    continue;
                }
                if (msp.startsWith("AlUsr~*~@")){
                    message = msp.substring(9, msp.length());
                    String all[] = message.split("&");
                    buddy = new HashSet<String>(Arrays.asList(all));
                }
                if (msp.startsWith("StsOf~*~@")){
                    message = msp.substring(9, msp.length());
                    String membs[] = message.split("&");
                    this.setUsers(membs);

                }
                if (msp.contains("MsgRx~*~@")){
                    int loc = msp.indexOf("MsgRx~*~@");
                    final String from = msp.substring(0, loc);
                    message = msp.substring(loc + 9, msp.length());
                    System.out.println("Available De Villiers got message : " + message);
                    if (!message.startsWith(userN + " said :"))
                        generateNotification(from, message);
                }
                if (msp.contains("RofM~*~@")){
                    String from;
                    String time;
                    String off_message;
                    Set<String> offline_call = new HashSet<String>();
                    Hashtable<String, List<String>> offline_map= new Hashtable<String, List<String>>();
                    System.out.println("Offline Message : " + msp);
                    ChatWindow cw;
                    String[] msg_set = msp.split("RofM[~][*][~][@]");
                    List<String> msgs;
                    for (String msg : msg_set){
                        try{
                            if (msg.equals(""))
                                continue;
                            System.out.println("Message : " + msg);
                            int loc = msg.indexOf('&');
                            from = msg.substring(0, loc);
                            int loc2 = msg.indexOf('&', loc + 1);
                            time = msg.substring(loc + 1, loc2);
                            off_message = msg.substring(loc2 + 1, msg.length());
                            /*if (offline_map.get(from) != null){
                                msgs = new ArrayList<String>();
                                msgs.add("Offline Message from " + from + " at " + time + "\n" + off_message + "\n");
                                offline_map.put(from, msgs);
                            }
                            else{
                                offline_map.get(from).add("Offline Message from " + from + " at " + time + "\n" + off_message + "\n");
                            }*/
                            generateNotification(from, "Offline Message from " + from + " at " + time + "\n" + off_message + "\n");

                                /*if (!offline_call.contains(from)){
                                    cw = new ChatWindow(userN, from, dout);
                                    offline_call.add(from);
                                    getChatWindow.put(from, cw);
                                    cw.processInMessage("Offline Message from " + from + " at " + time + "\n" + off_message + "\n");
                                    playSound();
                                }else{
                                    try{
                                        cw = (ChatWindow) getChatWindow.get(from);
                                        cw.processInMessage("Offline Message at " + time + "\n" + off_message + "\n");
                                        playSound();
                                    }
                                    catch (NullPointerException npe){
                                        System.out.println("Got Npe");
                                    }
                                }   */
                        }
                        catch(NullPointerException npe){
                            System.out.println("Got NPE");
                        }
                    }
                    /*Enumeration<String> e = offline_map.keys();
                    while(e.hasMoreElements()){
                        String user = e.nextElement();
                        List<String> msg= offline_map.get(user);
                        generateNotification(user, (String[])msg.toArray());
                    }*/

                }

        }

        /*while(!visible){
            if (mService == null)
                break;
            System.out.println("Service De Villiers Working here in non-visible mode");
            try {
                mService.readMessage();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                continue;
            }
        }*/
    }

    private void setUsers(String[] users){
        final String usrs[] = users;
        Set<String> online = new HashSet<String>(Arrays.asList(users));
        buddy.removeAll(online);
        if (buddy.contains(null))
            buddy.remove(null);
        if (buddy.contains(""))
            buddy.remove("");
        String buds[] = new String[buddy.size()];
        buddy.toArray(buds);
        final String budd[] = buds;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //To change body of implemented methods use File | Settings | File Templates.
                lvOnline.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, usrs));
                lvOffline.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, budd));
            }
        });
    }


    private class statusCheck extends TimerTask {
        public void run(){
            try {
                if (mService != null && visible){
                    mService.writeMessage("AlUsr@*@~");
                    mService.writeMessage("Status@*@~");
                }
                //textField1.setText(toUser);
                //if (dout!=null){
                //    dout.writeUTF("AlUsr@*@~");
                //    dout.writeUTF("Status@*@~");
                //}
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void initVars(){
        lvOnline = (ListView) findViewById(R.id.lvOnline);
        lvOffline = (ListView) findViewById(R.id.lvOffline);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //To change body of implemented methods use File | Settings | File Templates.
        //System.out.println("MyView = " + String.valueOf(view.getId()) + "Parent = " + String.valueOf(parent.getId()) + "Position = " + String.valueOf(position) + String.valueOf(R.id.lvOnline) + String.valueOf(R.id.lvOffline));
        //Intent chatWindow = new Intent(ctx, ChatWindow.class);
        Intent chatWindow = new Intent(ctx, TabbedChat.class);
        switch(parent.getId()){
            case R.id.lvOnline:
                toUser = (String) lvOnline.getItemAtPosition(position);
                break;
            case R.id.lvOffline:
                toUser = (String) lvOffline.getItemAtPosition(position);
                break;
        }
        chatWindow.putExtra("toUser", toUser);
        chatWindow.putExtra("UserName", userN);
        startActivity(chatWindow);
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        timer.cancel();
        visible = false;
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
