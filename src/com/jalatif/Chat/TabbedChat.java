package com.jalatif.Chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.Toast;
import com.viewpagerindicator.TitlePageIndicator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jalatif
 * Date: 4/17/13
 * Time: 10:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class TabbedChat extends FragmentActivity implements Runnable, ViewPager.OnPageChangeListener{

    private ChatPageAdapter pageAdapter;
    private Context ctx;
    private String userN = "";
    private String wUser = "";
    private Timer stsat = new Timer();
    boolean mBound = false;
    protected SocketService mService; //Jalatif Please remove static after trying
    private boolean visible = true;
    private boolean ouserStat = true;
    private Intent mIntent;
    //private Hashtable<String, ChatFragment> getChatFragment = new Hashtable<String, ChatFragment>();
    private ChatFragment cf;
    protected static List<String> titles = new ArrayList<String>();
    //private SoundPool notify;
    //private int soundID;
    private String notiMsg = "";
    private String msg_uuid = "bd320120-acc2-11e2-9e96-0800200c9a66";
    private int notifyId = 0;

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket mmSocket;
    private DataOutputStream mmOutputStream;
    private DataInputStream mmInputStream;
    private BluetoothDevice mmDevice;
    private boolean btConnection = false;
    Thread onliner;
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.chatview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ctx = getApplicationContext();
        if(mService != null)
            onNewIntent(getIntent());
        else{
        Bundle b = getIntent().getExtras();
        userN = b.getString("UserName");
        wUser = b.getString("toUser");
        if (b.containsKey("notification")){
            notiMsg = b.getString("notification");
        }
        else
            notiMsg = "";
        System.out.println("Tabbed Chat Notification Received = " + notiMsg);

        //NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //mNotificationManager.cancel(AvailableUsers.notifyId - 1);

        //setTitle("Chat B/w " + userN + " and " + wUser);
        setTitle(userN + "'s Chat");
        mIntent = new Intent(this, SocketService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        //btConnection = initBlueTooth();
        }
        /*int seconds = 2;
        stsat.schedule(new statusCheck(), 0, seconds * 1000);

        onliner = new Thread(this, "Tabb");
        onliner.start();

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);*/
        //notify = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        //soundID = notify.load(this, R.raw.doorbell, 1);

    }

    @Override
    protected void onStart() {
        super.onStart();    //To change body of overridden methods use File | Settings | File Templates.
        onliner = new Thread(this, "Tabb");
        onliner.start();
        int seconds = 2;
        stsat.schedule(new statusCheck(), 0, seconds * 1000);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);    //To change body of overridden methods use File | Settings | File Templates.
        Bundle b = intent.getExtras();
        userN = b.getString("UserName");
        wUser = b.getString("toUser");
        if (b.containsKey("notification"))
            notiMsg = b.getString("notification");
        else
            notiMsg = "";
        System.out.println("Tabbed Chat Notification Received = " + notiMsg);
        setTitle(userN + "'s Chat");
        //mIntent = new Intent(this, SocketService.class);
        //bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        if (mService.getChatFragment(wUser) == null){
            cf = new ChatFragment(wUser, mService, notiMsg);
            notiMsg = "";
            mService.putFragment(cf);
            mService.putChatFragment(wUser, cf);
            titles.add(0, wUser);
            pageAdapter.addItem(cf);
            //FragmentManager frm = this
            //FragmentTransaction ft = frm.beginTransaction();
            //ft.add(R.id.vpager, cf);
            //ft.commit();
                /*FragmentManager fragMgr = getSupportFragmentManager();
                FragmentTransaction fragTrans = fragMgr.beginTransaction();
                fragTrans.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
                fragTrans.replace(android.R.id.content, cf, wUser);
                fragTrans.addToBackStack(wUser);
                fragTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragTrans.commit();  */

        }
        else{
            mService.getChatFragment(wUser).setNotifyMsg(notiMsg);
            notiMsg = "";
        }


    }

/*private void playSound(){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        android.util.Log.v("SOUND","["+volume+"]["+notify.play(soundID, volume, volume, 1, 0, 1f)+"]");
    }*/


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            if (mService == null){
                SocketService.SocketBinder binder = (SocketService.SocketBinder) service;
                mService = binder.getService();
                try {
                    mService.writeMessage(userN + "SendTo@*@~" + "");
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                catch(NullPointerException npe){
                    System.out.println("Got An Npe : " + mService);
                }
                mBound = true;
                System.out.println("Service Connected to TabbedChat");
            }
            //dout = mService.getDout();//new SocketService().getDout();
            //din = mService.getDin();//new SocketService().getDin();
            //socket = mService.getSocket();//new SocketService().getSocket();
            //System.out.println("Jalatif Socket is " + socket);
            if (mService.getChatFragment(wUser) == null){
                cf = new ChatFragment(wUser, mService, notiMsg);
                notiMsg = "";
                mService.putFragment(cf);
                mService.putChatFragment(wUser, cf);
                titles.add(0, wUser);
                /*FragmentManager fragMgr = getSupportFragmentManager();
                FragmentTransaction fragTrans = fragMgr.beginTransaction();
                fragTrans.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
                fragTrans.replace(android.R.id.content, cf, wUser);
                fragTrans.addToBackStack(wUser);
                fragTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragTrans.commit();  */

            }
            else{
                mService.getChatFragment(wUser).setNotifyMsg(notiMsg);
                notiMsg = "";
            }
            /*cf = new ChatFragment("anime", mService);
            mService.putFragment(cf);
            getChatFragment.put("anime", cf);
            */
            pageAdapter = new ChatPageAdapter(getSupportFragmentManager(), mService.getFragments());//fragments);

            ViewPager pager = (ViewPager)findViewById(R.id.vpager);

            pager.setAdapter(pageAdapter);
            pager.setCurrentItem(titles.indexOf(wUser));

            //Bind the title indicator to the adapter
            TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.cvtitles);
            titleIndicator.setViewPager(pager);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    /*
    public String getActiveFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        //return (ChatFragment) getSupportFragmentManager().findFragmentByTag(tag);
        return tag;
    }
    */
    private boolean initBlueTooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return false;
        }
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        boolean connection = false;
        try {
            // mBluetoothAdapter.getRemoteDevice("00:12:11:30:08:44");
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("Robokits_Bluetooth")) {
                        mmDevice = device;
                        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                        mmSocket.connect();
                        mmOutputStream = new DataOutputStream(mmSocket.getOutputStream());
                        mmInputStream = new DataInputStream(mmSocket.getInputStream());
                        connection = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return connection;
    }
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
        //Notification noti = new Notification.Builder(ctx)
        Notification noti = new android.support.v4.app.NotificationCompat.Builder(ctx)
                .setContentTitle("New msg from " + from)
                .setContentText(msg)
                .setSmallIcon(R.drawable.talk_ldpi)
                .setContentIntent(pIntent)
                .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(msg)).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        //mService.playSound();
        notificationManager.notify(notifyId++, noti);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onPageSelected(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
        //setTitle(titles.get(i));
    }

    @Override
    public void onPageScrollStateChanged(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    private class statusCheck extends TimerTask {
        public void run(){
            try {
                if (mService != null && visible){
                    //mService.writeMessage("StsAt@*@~" + wUser);
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
            while (visible) {
                //dout.writeUTF("Status@*@~");
                if (mService == null)
                    continue;
                System.out.println("Working here");
                String msp = null;//din.readUTF();
                try {
                    msp = mService.readMessage();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    continue;
                }
                String message = "";
                System.out.println("De Villiers got message : " + msp);
                if (msp.contains("Heart~*~@")){
                    try {
                        mService.writeMessage("Heart@*@~");
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    continue;
                }
                if (msp.contains("MsgRx~*~@")){
                    int loc = msp.indexOf("MsgRx~*~@");
                    final String from = msp.substring(0, loc);
                    message = msp.substring(loc + 9, msp.length());
                    System.out.println("De Villiers got message : " + message);
                    if (message.startsWith(userN + " said :"))
                        continue;
                    final String uiMsg = message;
                    if (btConnection){
                        int loco = uiMsg.indexOf(':') + 1;
                        String process = uiMsg.substring(loco + 1, loco + 2).toUpperCase();
                        if (!process.equals(""))
                            try {
                                mmOutputStream.writeInt((int)process.charAt(0));
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                continue;
                            }
                    }
                    //String tag = getActiveFragment();
                    //if (!tag.equals(from)){
                    //    System.out.println("Gayle got message from not an active user " + tag + " but from " + from);
                          //generateNotification(from, uiMsg);
                    //}
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //To change body of implemented methods use File | Settings | File Templates.
                            if (!uiMsg.equals("")){
                                try{
                                    mService.getChatFragment(from).updateChat(uiMsg);
                                    mService.playSound();
                                }
                                catch(NullPointerException e){
                                    if (mService.getChatFragment(wUser) == null){
                                        cf = new ChatFragment(wUser, mService);
                                        mService.putFragment(cf);
                                        mService.putChatFragment(wUser, cf);
                                        titles.add(0, wUser);
                                        try {
                                            mService.writeMessage("");
                                        } catch (IOException e1) {
                                            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                        }
                                    }
                                }
                            }
                                //getChatFragment.get(from).updateChat(uiMsg);//talk.insert(uiMsg, 0);
                            //talk.add(uiMsg);
                        }
                    });
                }
                /*if(msp.startsWith("StsAt~*~@")){
                    final String status = msp.substring(9, msp.length());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //To change body of implemented methods use File | Settings | File Templates.
                            if (status.equals("true")){
                                //tvStatus.setText("Online");
                                //ouserStat = true;
                            }
                            else{
                                //tvStatus.setText("Offline");
                                //ouserStat = false;
                            }
                        }
                    });
                }*/
                if (msp.startsWith("StsOf~*~@")){
                    message = msp.substring(9, msp.length());
                    String membs[] = message.split("&");
                    Set<String> checkin = new HashSet<String>(Arrays.asList(membs));
                    //this.setUsers(membs);
                    Enumeration<String> e = mService.getUsers();//getChatFragment.keys();
                    while(e.hasMoreElements()){
                        String stt = "";
                        final String checkUser = e.nextElement();
                        if (checkin.contains(checkUser))
                            stt = "true";
                        else
                            stt = "false";
                        final String status = stt;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //To change body of implemented methods use File | Settings | File Templates.
                                if (status.equals("true")){
                                    mService.getChatFragment(checkUser).updateStatus(true);//getChatFragment.get(checkUser).updateStatus(true);
                                    //tvStatus.setText("Online");
                                    //ouserStat = true;
                                }
                                else{
                                    mService.getChatFragment(checkUser).updateStatus(false);//getChatFragment.get(checkUser).updateStatus(false);
                                    //tvStatus.setText("Offline");
                                    //ouserStat = false;
                                }
                            }
                        });

                    }
                }
            }

            /* Skype Check
            while(!visible){
                if (mService == null)
                    break;
                //System.out.println("Service De Villiers Working here in non-visible mode");
                try {
                    mService.readMessage();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    continue;
                }
            }  */

    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        stsat.cancel();
        visible = false;
        //Skype Check
        /*if (mBound){
            unbindService(mConnection);
            mBound = false;
        } */
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
