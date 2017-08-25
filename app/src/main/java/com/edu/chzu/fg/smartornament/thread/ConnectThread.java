package com.edu.chzu.fg.smartornament.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;


import java.io.IOException;
import java.util.UUID;

import static com.edu.chzu.fg.smartornament.fragment.LinkDeviceFragment.TASK_GET_SOCKET;
import static com.edu.chzu.fg.smartornament.fragment.LinkDeviceFragment.TASK_GET_SOCKET_FAILED;

/**
 * Created by FG on 2016/12/30.
 */

public class ConnectThread extends Thread {
    // UUID号，表示不同的数据协议
    private final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter, Handler handler) {
        mBluetoothAdapter=bluetoothAdapter;
        BluetoothSocket tmp = null;
        mDevice = device;
        mHandler=handler;
        try {
            //创建RFCOMM通道
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(UUID_STR));
        } catch (IOException e) {
//            Log.d(TAG, "createRfcommSocketToServiceRecord error!");
        }

        mSocket = tmp;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void run() {
        Message handlerMsg = mHandler.obtainMessage();
        // 取消设备扫描，都开始连接了，还扫个毛！
        mBluetoothAdapter.cancelDiscovery();
        try {
            // 连接远程服务器设备，另一方的服务端会收到连接请求
            mSocket.connect();

            handlerMsg.what = TASK_GET_SOCKET;
            handlerMsg.obj = mSocket;
            mHandler.sendMessage(handlerMsg);
        } catch (IOException connectException) {
//            Log.e(TAG, "Connect server failed");
            handlerMsg.what=TASK_GET_SOCKET_FAILED;
            handlerMsg.obj="failed";
            mHandler.sendMessage(handlerMsg);
            try {
                mSocket.close();
            } catch (IOException closeException) {}
            // 连接服务器失败，则自己作为服务器监听
//            mAcceptThread = new MainActivity.AcceptThread();
//            mAcceptThread.start();
            return;
        }

//        mConnectedThread=new ConnectedThread(mSocket,mHandler);
//        mConnectedThread.start();
    }

}
