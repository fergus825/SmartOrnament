package com.edu.chzu.fg.smartornament.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by FG on 2016/12/30.
 */

public class AcceptThread extends Thread {
    // UUID号，表示不同的数据协议
    private final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
    private final BluetoothServerSocket mServerSocket;
    private boolean isCancel = false;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    public ConnectedThread mConnectedThread;
    public AcceptThread(BluetoothAdapter bluetoothAdapter, Handler handler) {
        mBluetoothAdapter=bluetoothAdapter;
        BluetoothServerSocket tmp = null;
        mHandler=handler;
        try {
            //监听RFCOMM
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    "Bluetooth_Chat_Room", UUID.fromString(UUID_STR));
        } catch (IOException e) {

        }
        mServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                // 阻塞等待
                socket = mServerSocket.accept(); //监听的socket接收连接请求后，返回一个BluetoothSocket
            } catch (IOException e) {
                if (!isCancel) {
                    try {
                        mServerSocket.close();
                    } catch (IOException e1) {

                    }
                    // 异常结束时，再次监听
                   // mAcceptThread = new MainActivity.AcceptThread();
                   // mAcceptThread.start();
                    //this.start();不知道可行；
                }
                break;
            }
            if (socket != null) {  //如果监听请求连接的Socket已经建立了
                // 管理已经连接的客户端******************************************************
              //  mConnectedThread=new ConnectedThread(socket,mHandler);
                mConnectedThread.start();
                try {
                    mServerSocket.close();  //那么就能关闭监听的socker了
                } catch (IOException e) {

                }
               // mAcceptThread = null;又是释放资源
                break;
            }
        }
    }
    /** Will cancel the listening socket, and cause the thread to finish */
//    public void cancel() {
//        try {
//            isCancel = true;
//            mServerSocket.close();
//            mAcceptThread = null;
//            if (mConnThread != null && mConnThread.isAlive()) {
//                mConnThread.cancel();
//            }
//        } catch (IOException e) {
//
//        }还得传入一个连接线程，不够麻烦的！
//    }
}
