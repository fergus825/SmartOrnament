package com.edu.chzu.fg.smartornament.service;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.edu.chzu.fg.smartornament.activity.MainActivity;
import com.edu.chzu.fg.smartornament.application.MyApplication;
import com.edu.chzu.fg.smartornament.thread.AcceptThread;
import com.edu.chzu.fg.smartornament.thread.ConnectedThread;
import com.edu.chzu.fg.smartornament.utils.LocationUtils;
import com.edu.chzu.fg.smartornament.utils.SosUtils;

import static cn.bmob.v3.Bmob.getApplicationContext;

/**
 * Created by FG on 2017/1/6.
 */

public class BtService extends Service{
    private AcceptThread mAcceptThread;            //作为服务端监听线程
    private ConnectedThread mangerThread;          //作为客服端请求线程
    private MyApplication application;
    private BluetoothSocket bluetoothSocket;
    private LocationUtils locationUtils;
    private Vibrator vibrator;
    private AssetManager assetManager;
    private Intent shakeService;
    private boolean isServiceStart=false;
    private MyApplication mApplication;

    private SosUtils sosUtils;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sosUtils=new SosUtils(getApplicationContext());
        locationUtils=new LocationUtils(getApplicationContext());
        vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        application= (MyApplication) getApplication();
        bluetoothSocket=application.getBluetoothSocket();
        mangerThread= new ConnectedThread(bluetoothSocket,getApplicationContext(),application.getHandler());
        mangerThread.start();
        mangerThread.write("1");//写入1，让智能挂饰开始工作
        Log.i("Test2222", "蓝牙数据处理服务成功开启，发送指令1让让挂饰开始工作");
        mangerThread.recieve(new ConnectedThread.OnReusltListener() {
            @Override
            public void handlerBLEmsg(String bleData) {
                //Log.e("eeeeeee",bleData);//这里不能显示toast，因为是子线程，但是处理数据还是可以的,需要用handler.post()
               // Toast.makeText(getApplicationContext(),bleData,Toast.LENGTH_SHORT).show();
                if (!application.getPhoneState()){
                    handleReceivedBleMsg(bleData);
                }
            }
            private void handleReceivedBleMsg(String rcv) {
                if (rcv.equals("1")){
                    Log.i("Test2222", "接收到指令1，手机执行振动操作");
                    Toast.makeText(getApplicationContext(), "蓝色警报——手机振动", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(500);//手机振动
                }else if (rcv.equals("2")){
                    Log.i("Test2222", "接收到指令2，手机执行振动并响铃操作");
                    Toast.makeText(getApplicationContext(), "黄色警报——手机振动并响铃", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(1000);//手机振动
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            playRing();
                        }
                    }).start();

                }else if (rcv.equals("3")){
                    //紧急状态，定位，发送短信并拨通电话
                    Log.i("Test4444", "接收到指令3，手机开启百度定位来获取当前位置");
                    locationUtils.startLocation();
                    if (!isServiceStart) {
                        Log.i("Test2222", "接收到指令3，开启摇一摇求助服务");
                        shakeService = new Intent();//开启摇一摇打电话的功能
                        shakeService.setClass(getApplicationContext(), ShakeService.class);
                        getApplicationContext().startService(shakeService);
                        Toast.makeText(getApplicationContext(), "橙色警报--摇一摇求助功能已经开启", Toast.LENGTH_SHORT).show();
                        isServiceStart=true;
                    }
                }else if (rcv.equals("4")){
                   /* Log.i("Test4444", "接收到指令4，手机开启百度定位来获取当前位置");

                    vibrator.vibrate(2000);//手机振动
                    Log.i("Test2222", "接收到指令4，手机开始执行自动求救操作");
                    Log.i("Test6666","接收到指令4，手机自动拨打联系人电话和发送求救短信");*/
                    Toast.makeText(getApplicationContext(), "红色警报--自动发定位短信和拨打电话", Toast.LENGTH_SHORT);
                    sosUtils.callForHelp();
                }
            }


        });


        IntentFilter filter = new IntentFilter();
        filter.addAction("PAUSE_SERVICE");
        filter.addAction("RESTART_SERVICE");
        filter.addAction("STOP_SERVICE");
        filter.addAction("CUT_CONNECT");
        getApplicationContext().registerReceiver(mReceiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mangerThread.close();
        mangerThread=null;
        application.setBluetoothSocket(null);//通信连接置为空
        getApplicationContext().unregisterReceiver(mReceiver);
    }
    private void playRing() {
        try {
            MediaPlayer player=null;
            assetManager = getApplicationContext().getAssets();
            AssetFileDescriptor afd = assetManager.openFd("caution.mp3");
            player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private final BroadcastReceiver mReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent  stopService=new Intent();
            String action = intent.getAction();
            if ("PAUSE_SERVICE".equals(action)){
                Toast.makeText(getApplicationContext(),"暂停服务",Toast.LENGTH_SHORT).show();
                mangerThread.write("0");
                mangerThread.pause();//暂停管理线程
            }else if ("STOP_SERVICE".equals(action)){
                Toast.makeText(getApplicationContext(),"停止服务",Toast.LENGTH_SHORT).show();
                stopService.setClass(getApplicationContext(), ShakeService.class);
                getApplicationContext().stopService(stopService);//关闭摇一摇服务
                mangerThread.write("0"); //关闭挂饰
                application.setBluetoothSocket(null);//通信连接置为空
            }else if ("CUT_CONNECT".equals(action)){
                Toast.makeText(getApplicationContext(),"断开连接",Toast.LENGTH_SHORT).show();
                mangerThread.write("0");
                mangerThread.close();  //关闭管理线程
                application.setBluetoothSocket(null);//通信连接置为空
            }else if ("RESTART_SERVICE".equals(action)){
                Toast.makeText(getApplicationContext(),"重启服务",Toast.LENGTH_SHORT).show();
                mangerThread.reStart();
                mangerThread.write("1");
            }
        }
    };
}
