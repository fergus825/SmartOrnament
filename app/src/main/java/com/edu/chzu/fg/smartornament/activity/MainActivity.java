package com.edu.chzu.fg.smartornament.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edu.chzu.fg.smartornament.R;
import com.edu.chzu.fg.smartornament.animation.GuillotineAnimation;
import com.edu.chzu.fg.smartornament.application.MyApplication;
import com.edu.chzu.fg.smartornament.fragment.GuardFragment;
import com.edu.chzu.fg.smartornament.fragment.LinkDeviceFragment;
import com.edu.chzu.fg.smartornament.fragment.SettingFragment;
import com.edu.chzu.fg.smartornament.fragment.SosFragment;
import com.edu.chzu.fg.smartornament.service.BtService;
import com.edu.chzu.fg.smartornament.service.ShakeService;
import com.edu.chzu.fg.smartornament.thread.AcceptThread;
import com.edu.chzu.fg.smartornament.thread.ConnectThread;
import com.edu.chzu.fg.smartornament.utils.LocationUtils;
import java.util.ArrayList;
import java.util.Set;

import static android.view.Gravity.LEFT;
import static android.view.Gravity.RIGHT;
import static com.edu.chzu.fg.smartornament.fragment.LinkDeviceFragment.TASK_RECV_MSG;

public class MainActivity extends AppCompatActivity {
    private Fragment linkDevice,guardFragment, sosFragment, settingFragment;
    private static final long RIPPLE_DURATION = 250;
    private Toolbar toolbar;
    private FrameLayout root;
    private View contentHamburger;
    private GuillotineAnimation mAnimation;
    private TextView txt_title;
    private  LocationUtils locationUtils;
    private Vibrator vibrator;
    private AssetManager assetManager;
    private boolean isCalling=false;
    private MyApplication mApplication;
    private boolean isServiceStart=false;
    private GestureDetector gestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
        root.addView(guillotineMenu);
        /*if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(null);
        }*/
        mAnimation=new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger)
                .setStartDelay(RIPPLE_DURATION)
                .setActionBarViewForAnimation(toolbar)
                .setClosedOnStart(true)
                .build();

        gestureDetector=new GestureDetector(this,onGestureListener);
        setChecked(0);


        IntentFilter filter = new IntentFilter();
        filter.addAction("CUT_CONNECT");
        registerReceiver(mReceiver, filter);
    }

    private void initView() {
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        root= (FrameLayout) findViewById(R.id.rootview);
        contentHamburger=findViewById(R.id.content_hamburger);
        txt_title= (TextView) findViewById(R.id.txt_title);
        mApplication= (MyApplication) getApplication();
    }


    public  void onClick(View view){
        switch (view.getId()){
            case R.id.btn_linkble:
                setChecked(0);
                txt_title.setText("设备连接");
                mAnimation.close();
                break;
            case R.id.btn_protect:
                setChecked(1);
                txt_title.setText("侦测模式");
                mAnimation.close();
                break;
            case R.id.btn_sos:
                setChecked(2);
                txt_title.setText("求助模式");
                mAnimation.close();
                break;
            case R.id.btn_settings:
                setChecked(3);
                txt_title.setText("系统设置");
               mAnimation.close();
                break;

        }
    }

    private GestureDetector.OnGestureListener onGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    float x = e2.getX() - e1.getX();
                    float y = e2.getY() - e1.getY();
                    if (x > 0) {
                        doResult(RIGHT);
                    } else if (x < 0) {
                        doResult(LEFT);
                    }
                    return true;
                }
            };

    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void doResult(int action) {

        switch (action) {
            case RIGHT:
                System.out.println("go right");
                if (mAnimation.isOpening()){//close 都无反应  !close打开：可以右滑动关闭，关闭：有重复动画
                    // open都无反应   ！isOpen打开：可以右滑动关闭，关闭：有重复动画
                    mAnimation.close();
                }
                break;

            case LEFT:
                System.out.println("go left");
                if (mAnimation.isClosing()){
                    mAnimation.open();
                }
                break;

        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);//true对任何Activity都适用,false表示只对第一个启动的Acitivity有用
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


//双击返回键退出app
//    private long exitTime = 0;
//    public void ExitApp()
//    {
//        if ((System.currentTimeMillis() - exitTime) > 2000)
//        {
//            Toast.makeText(this.activity, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//            exitTime = System.currentTimeMillis();
//        } else
//        {     stopService(intentService);//按返回键会停止该服务，并且关闭通信socket，和通信线程
//            this.activity.finish();
//        }
//    }


    private void setChecked(int i) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        //=======================================================================
        // 1.隐藏已将加载过的Fragment
        //=======================================================================
        if (linkDevice != null) {
            transaction.hide(linkDevice);
        }
        if (guardFragment != null) {
            transaction.hide(guardFragment);
        }
        if (sosFragment != null) {
            transaction.hide(sosFragment);
        }
        if (settingFragment != null) {
            transaction.hide(settingFragment);
        }

        //=======================================================================
        //2. 开始加载当前选中的Framgment
        //=======================================================================
        switch (i) {
            case 0:
                if (linkDevice==null){
                    linkDevice=new LinkDeviceFragment();
                    transaction.add(R.id.fl_container,linkDevice);
                }else {
                    transaction.show(linkDevice);
                }
                break;
            case 1:
                if (guardFragment == null) {
                    //第一次加载
                    guardFragment = new GuardFragment();
                    transaction.add(R.id.fl_container, guardFragment);
                } else {
                    transaction.show(guardFragment);
                }
                break;
            case 2:
                if (sosFragment == null) {
                    sosFragment = new SosFragment();
                    transaction.add(R.id.fl_container, sosFragment);
                } else {
                    transaction.show(sosFragment);
                }
                break;
            case 3:
                if (settingFragment == null) {
                    settingFragment = new SettingFragment();
                    transaction.add(R.id.fl_container, settingFragment);
                } else {
                    transaction.show(settingFragment);
                }
                break;

        }

        transaction.commit();
    }




    @Override
    protected void onRestart() {
        super.onRestart();
        isCalling=true;
        mApplication.setPhoneState(isCalling);
        Log.i("55555","onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCalling=false;
        mApplication.setPhoneState(isCalling);
        Log.i("55555","onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        isCalling=true;
        mApplication.setPhoneState(isCalling);
        Log.i("55555","onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isCalling=false;
        mApplication.setPhoneState(isCalling);
        Log.i("55555","onStop");
    }
    private final BroadcastReceiver mReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
           if ("CUT_CONNECT".equals(action)){
                Toast.makeText(getApplicationContext(),"跳转至设备连接界面",Toast.LENGTH_SHORT).show();
                setChecked(0);
            }
        }
    };
}

