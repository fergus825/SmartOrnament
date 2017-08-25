package com.edu.chzu.fg.smartornament.service;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.edu.chzu.fg.smartornament.sql.dao.SosContacterDAO;
import com.edu.chzu.fg.smartornament.sql.model.SosContacter;
import com.edu.chzu.fg.smartornament.utils.SosUtils;

import java.util.List;

/**
 * Created by FG on 2016/11/23.
 */

public class ShakeService extends Service {
    public static final String TAG = "ShakeService";
    private SensorManager mSensorManager;
    private SensorManager sensorManager;
    private Vibrator vibrator;

    private static final int SENSOR_SHAKE = 10;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS=2;

    private SharedPreferences prefs,preferences ;
    private SharedPreferences.Editor editor;
    private SosContacter mSosContacter;
    private SosContacterDAO mSosContacterDAO;


    private SosUtils sosUtils;
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        sosUtils=new SosUtils(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

        if (sensorManager != null) {// 注册监听器
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // 传感器信息改变时执行该方法
            float[] values = event.values;
            float x = values[0]; // x轴方向的重力加速度，向右为正
            float y = values[1]; // y轴方向的重力加速度，向前为正
            float z = values[2]; // z轴方向的重力加速度，向上为正
            //	Log.i(TAG, "x轴方向的重力加速度" + x +  "；y轴方向的重力加速度" + y +  "；z轴方向的重力加速度" + z);
            // 一般在这三个方向的重力加速度达到40就达到了摇晃手机的状态。
            int medumValue = 30;
            if (Math.abs(x) > medumValue || Math.abs(y) > medumValue || Math.abs(z) > medumValue) {
                Log.i("Test5555", "x轴方向的重力加速度" + x + "；y轴方向的重力加速度" + y + "；z轴方向的重力加速度" + z);
                vibrator.vibrate(200);
                Message msg = new Message();
                msg.what = SENSOR_SHAKE;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO 自动生成的方法存根

        }
    };


    /**
     * 处理监听器发送的信息，触发相应动作的执行*/

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SENSOR_SHAKE:
                    sosUtils.callForHelp();
                    Toast.makeText(getApplicationContext(), "短信发送成功！", Toast.LENGTH_SHORT).show();
                    Log.i("Test6666","检测到手机摇晃，手机自动拨打联系人电话和发送求救短信");
                    break;
            }
        }

    };


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
