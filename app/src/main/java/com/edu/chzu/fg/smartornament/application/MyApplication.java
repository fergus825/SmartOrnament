package com.edu.chzu.fg.smartornament.application;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

/**
 * Created by FG on 2017/1/6.
 */

public class MyApplication extends Application{
    private BluetoothSocket bluetoothSocket;
    private Handler handler;
    private boolean isCalling;
    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothSocket=null;
        handler=null;
    }
    public void setBluetoothSocket(BluetoothSocket bluetoothSocket){
        this.bluetoothSocket=bluetoothSocket;
    }
    public BluetoothSocket getBluetoothSocket(){
        return bluetoothSocket;
    }

    public void setHandler(Handler handler){
        this.handler=handler;
    }
    public Handler getHandler(){
        return handler;
    }
    public void setPhoneState(boolean isCalling){
        this.isCalling=isCalling;
    }
    public boolean getPhoneState(){
        return isCalling;
    }
}
