package com.edu.chzu.fg.smartornament.thread;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.edu.chzu.fg.smartornament.activity.MainActivity;
import com.edu.chzu.fg.smartornament.service.ShakeService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.edu.chzu.fg.smartornament.fragment.LinkDeviceFragment.TASK_DISCONNECT;
import static com.edu.chzu.fg.smartornament.fragment.LinkDeviceFragment.TASK_RECV_MSG;

/**
 * Created by FG on 2016/12/30.
 */

public class ConnectedThread extends Thread {
    ///public static final int TASK_RECV_MSG = 2;
    private final BluetoothSocket mSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;
    private BufferedWriter mBw;
    private Context context;
    private Vibrator vibrator;
    private boolean isOpen=true;
    boolean suspended=false;
    private Handler mHandler;

    private OnReusltListener mOnResultListener;

    public ConnectedThread(BluetoothSocket socket, Context context,Handler handler) {
        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        mSocket = socket;//被管理的Socket
        mHandler=handler;
        this.context=context;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        //无论是客户端的Socket还是服务端的Socket，都有输入输出流
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {  }
        mInStream = tmpIn;
        mOutStream = tmpOut;
        // 获得远程设备的输出缓存字符流，相当于找了一个大的管道，可以通向远程设备，发数据的必经管道
        mBw = new BufferedWriter(new PrintWriter(mOutStream));

        //mHandler=new Handler(Looper.getMainLooper());
    }

    public OutputStream getOutputStream() {
        return mOutStream;
    }

    public boolean write(String msg) {
        if (msg == null)
            return false;
        try {
            mBw.write(msg + "\n");
            mBw.flush();
            System.out.println("Write:" + msg);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    //获取远程设备的名字
    public String getRemoteName() {
        return mSocket.getRemoteDevice().getName();
    }

//    public void cancel() {
//        try {
//            mSocket.close();
//        } catch (IOException e) {
//        }
//        mConnThread = null;
//    }还得传入一个连接线程的全局变量，他也就得从服务端或客户端线程传入两次才得传入，老子不要这个取消连接功能，行不？

    public void run() {//该线程启动后，自动进行的操作就是读取数据流。而发消息就得手动调用方法了
        android.os.Message handlerMsg;
        String buffer = null;
        String lastString="0";//用来存储接收的数据，比对现在收到的数据和上次接收的数据是否一样，一样则忽略
        // 获得远程设备的缓存字符输入流
        BufferedReader br = new BufferedReader(new InputStreamReader(
                mInStream));

        while (isOpen) {
            try {
                // 读取远程设备的一行数据
                buffer = br.readLine();
                System.out.println("收到：" + buffer);
                if (buffer == null)
                    continue;
                // 把接收到的数据发送到MainActivity上，便于进一步处理，
                // 下一步打算用接口回调到专门处理信息的service中，对数据进行分析处理
                handlerMsg= mHandler.obtainMessage();
                handlerMsg.what =TASK_RECV_MSG;
                handlerMsg.obj = buffer;
                final String s=buffer;
               // mHandler.sendMessage(handlerMsg);
                if(!lastString.equals(s)){//如果收到的数据和上次的不一样，则接受处理，否则忽略
                    mHandler.post(new Runnable() { //把数据提交到主线程，没有post方法，回调方就不能在回调方法中操作ui
                        @Override
                        public void run() {
                            if (mOnResultListener!=null)
                                mOnResultListener.handlerBLEmsg(s);
                        }
                    });
                    lastString=s;
                }

              /*  if (mOnResultListener!=null)留用
                    mOnResultListener.handlerBLEmsg(buffer);*/

                /*下面是用于测试，收到1，则启动摇一摇打电话的功能*/
               /* if (buffer.equals("1")){
                    vibrator.vibrate(200);
                    Intent service=new Intent();
                    service.setClass(context,ShakeService.class);
                    context.startService(service);
                }*/
            } catch (IOException e) {
                try {
                    mSocket.close();
                } catch (IOException e1) {

                }
//                mConnThread = null;出现异常，释放线程所占的资源
                break;
            }
               //每个循环都检测线程是否被暂停
            synchronized(this) {
                while(suspended) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Toast.makeText(context,"暂停侦测服务。。。",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }

        }
    }
    public void close(){
        Message handlerMsg=mHandler.obtainMessage();
        Log.i("77777","服务关闭‘’‘’");
        isOpen=false; //停止线程中的接收消息的循环
        try {
            mSocket.close();  //关闭通信的socket
        } catch (IOException e) {
            e.printStackTrace();
        }
        //手动断开蓝牙通信连接，告知LinkDeviceFragment
        handlerMsg.what=TASK_DISCONNECT;
        handlerMsg.obj=null;
        mHandler.sendMessage(handlerMsg);

    }
    /**
     * 暂停
     */
    public void pause(){
        suspended = true;
    }

    /**
     * 继续
     */
    synchronized public void reStart(){
        suspended = false;
        notify();
    }
    public interface OnReusltListener {
        void handlerBLEmsg(String bleData);
    }

    public void recieve(OnReusltListener listener){
        mOnResultListener=listener;
    }
}
