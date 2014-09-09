package com.jalatif.Chat;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: jalatif
 * Date: 4/14/13
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class MySocket implements Parcelable {

    private Socket socket;

    public MySocket(Socket socket) {
        this.socket = socket;
    }

    protected Socket getSocket(){
        return socket;
    }
    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
