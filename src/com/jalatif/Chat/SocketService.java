package com.jalatif.Chat;


import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: jalatif
 * Date: 4/16/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class SocketService extends Service {
    private String host = "";
    private int port ;
    private String logTxt = "";
    private boolean connected = false;
    private Socket socket;
    private DataOutputStream dout;
    private DataInputStream din;
    private int result = Activity.RESULT_CANCELED;
    private Context ctx;
    private List<Fragment> fragment_list= new ArrayList<Fragment>();
    private Hashtable<String, ChatFragment> userChatMap = new Hashtable<String, ChatFragment>();
    private SoundPool notify;
    private int soundID;
    private UIHandler uiHandler;
    private String userN;
    private String userP;
    private boolean makeConnectionCalled = false;
    private Timer timer;

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket mmSocket;
    private DataOutputStream mmOutputStream;
    private DataInputStream mmInputStream;
    private BluetoothDevice mmDevice;
    private boolean btConnection = false;
    Thread onliner;
    private final UUID uuide = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    IBinder mBinder = new SocketBinder();

    public class SocketBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }
    public SocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;  //To change body of implemented methods use File | Settings | File Templates.
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        host = extras.getString("Host");
        port = extras.getInt("Port");
        ctx = this;
        System.out.println("Trying Connection to " + host + " port = " + String.valueOf(port));
        connected = false;
        MyTask mt = new MyTask();
        mt.execute();
        try {
            mt.get(5000, TimeUnit.MILLISECONDS);
            System.out.println("Connected = " + connected + logTxt + " Socket = " + socket);
            int seconds = 3;
            if (timer == null){
                timer = new Timer();
                timer.schedule(new heartBeatSend(), 0, seconds*1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TimeoutException e) {
            mt.cancel(true);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            showToast("Connection Timeout");
        }
        //Toast.makeText(this, logTxt, 500);
        Messenger messenger = (Messenger) extras.get("MESSENGER");
        Message msg = Message.obtain();
        msg.arg1 = result;
        msg.obj = logTxt;
        try {
            messenger.send(msg);
            initSound();
            btConnection = initBlueTooth();
        } catch (RemoteException e1) {
            Log.w(getClass().getName(), "Exception sending message", e1);
        }

        return super.onStartCommand(intent, flags, startId);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void showToast(String log){
        Toast.makeText(ctx, log, Toast.LENGTH_SHORT).show();
    }
    protected class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);    //To change body of overridden methods use File | Settings | File Templates.
            if (uiHandler == null)
                showToast(logTxt);
            else
                handleUIRequest(logTxt);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                System.out.println("Trying Connection to " + host + " port = " + String.valueOf(port));
                socket = new Socket( host, port );
                dout = new DataOutputStream(socket.getOutputStream());
                din = new DataInputStream(socket.getInputStream());
                result = Activity.RESULT_OK;
                logTxt = "Connection can be made";
                connected = true;
            }
            catch(UnknownHostException uhe){
                //logTxt = "Unknow host : " + host +" Check Internet";
                logTxt = "Unknow host -> Check Internet or Server is Down";
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
            System.out.println("Socket = " + socket + "Logtxt = " + logTxt);
            publishProgress();
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private class heartBeatSend extends TimerTask {
        public void run(){
            try {
                if (socket != null){
                    writeMessage("Heart@*@~");
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    protected void checkConnection(){
        if (makeConnectionCalled)
            return;
        makeConnectionCalled = true;
        if (uiHandler == null){
            HandlerThread uiThread = new HandlerThread("UIHandler");
            uiThread.start();
            uiHandler = new UIHandler(uiThread.getLooper());
        }
        if (!isNetworkConnected()){
            handleUIRequest("Connection Lost.. Check Internet..");
            SystemClock.sleep(2000);
            makeConnectionCalled = false;
            return;
        }
        System.out.println("Ezio Problem Found in checkConnection and trying to solve");
        makeConnectionCalled = true;
        socket = null;
        dout = null;
        din = null;
        connected = false;
        //HandlerThread uiThread = new HandlerThread("UIHandler");
        //uiThread.start();
        //uiHandler = new UIHandler(uiThread.getLooper());

        //handleUIRequest("Connection Lost.. Check Internet.. Trying again");
        handleUIRequest("Internet Found ... Attempting to Connect Again");
        //Toast.makeText(ctx, "Connection Lost.. Check Internet.. Trying again", Toast.LENGTH_LONG).show();
        MyTask mt = new MyTask();
        mt.execute();
        try {
            mt.get(8000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TimeoutException e) {
            mt.cancel(true);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            //showToast("Connection Timeout");
            handleUIRequest("Connection Timeout");
        }
        if (!connected){
            makeConnectionCalled = false;
            return;
        }
        handleUIRequest("Connected Again");
        try {
            writeMessage(userN + "AuthU@*@~" + userP);
            //readMessage();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        makeConnectionCalled = false;
        //System.out.println("Connected = " + connected + logTxt + " Socket = " + socket);
    }

    protected DataOutputStream getDout(){
        return dout;
    }

    protected DataInputStream getDin(){
        return din;
    }

    protected Socket getSocket(){
        return socket;
    }

    protected void writeMessage(String msg) throws IOException{
        try {
            if (dout != null)
                dout.writeUTF(msg);
            else
                throw new IOException("");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.out.println("Ezio Problem Found in writing in Socket Service");
            checkConnection();
            throw new IOException("Can't Write Connection Lost");
        }

    }

    protected String readMessage()  throws IOException {
        try {
            if (din != null){
                String msg = din.readUTF();
                if (msg.contains("MsgRx~*~@"))
                    btSend(msg);
                return msg;
            }
            else
                throw new IOException("");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.out.println("Ezio Problem Found in reading in Socket Service");
            checkConnection();
            throw new IOException("Can't Read Connection Lost");
        }
    }

    protected void putFragment(Fragment f){
        //fragment_list.add(f);
        fragment_list.add(0, f);
    }

    protected Fragment getFragmentAt(int position){
        return fragment_list.get(position);
    }

    protected List<Fragment> getFragments(){
        return fragment_list;
    }

    protected Boolean removeFragment(Fragment f){
        return fragment_list.remove(f);
    }


    protected void putChatFragment(String user, ChatFragment cf){
        userChatMap.put(user, cf);
    }

    protected ChatFragment getChatFragment(String user){
        return userChatMap.get(user);
    }

    protected Enumeration<String> getUsers(){
        return userChatMap.keys();
    }

    protected void setDetails(String un, String up){
        this.userN = un;
        this.userP = up;
    }

    private void initSound(){
        notify = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundID = notify.load(this, R.raw.doorbell, 1);
    }

    protected void playSound(){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        android.util.Log.v("SOUND","["+volume+"]["+notify.play(soundID, volume, volume, 1, 0, 1f)+"]");
    }

    private final class UIHandler extends Handler
    {
        public static final int DISPLAY_UI_TOAST = 0;
        public static final int DISPLAY_UI_DIALOG = 1;

        public UIHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case UIHandler.DISPLAY_UI_TOAST:
                {
                    Context context = getApplicationContext();
                    Toast t = Toast.makeText(context, (String)msg.obj, Toast.LENGTH_LONG);
                    t.show();
                }
                case UIHandler.DISPLAY_UI_DIALOG:
                    //TBD
                default:
                    break;
            }
        }

    }

    private void handleUIRequest(String message)
    {
        Message msg = uiHandler.obtainMessage(UIHandler.DISPLAY_UI_TOAST);
        msg.obj = message;
        uiHandler.sendMessage(msg);
    }

    private void btSend(String msp){
        int loc = msp.indexOf("MsgRx~*~@");
        final String from = msp.substring(0, loc);
        String message = msp.substring(loc + 9, msp.length());
        System.out.println("Service De Villiers got message : " + message + " from " + from);
        if (message.startsWith(userN + " said :"))
            return;
        final String uiMsg = message;

        //if (!btConnection)
        //    initBlueTooth();
        System.out.println("Service De Villiers Connection = " + btConnection);// + " OutputStream " + mmOutputStream.toString());
        if (btConnection){
            int loco = uiMsg.indexOf(':') + 1;
            String process = uiMsg.substring(loco + 1, loco + 2).toUpperCase();
            if (!process.equals(""))
                try {
                    mmOutputStream.writeInt((int)process.charAt(0));
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
        }

    }

    private boolean initBlueTooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return false;
        }

        System.out.println("De Villiers Service Bluetooth Working");
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        boolean connection = false;
        try {
            // mBluetoothAdapter.getRemoteDevice("00:12:11:30:08:44");
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().contains("Robokits_Bluetooth")) {
                        mmDevice = device;
                        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuide);
                        mmSocket.connect();
                        mmOutputStream = new DataOutputStream(mmSocket.getOutputStream());
                        mmInputStream = new DataInputStream(mmSocket.getInputStream());
                        connection = true;
                        System.out.println("De Villiers Service Bluetooth Connected");
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

    private String whichVisible(){

        return "";
    }

    private void setVisiblity(String active){

    }


}
