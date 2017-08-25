package com.edu.chzu.fg.smartornament.fragment;

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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.edu.chzu.fg.smartornament.R;
import com.edu.chzu.fg.smartornament.application.MyApplication;
import com.edu.chzu.fg.smartornament.service.ShakeService;
import com.edu.chzu.fg.smartornament.thread.AcceptThread;
import com.edu.chzu.fg.smartornament.thread.ConnectThread;
import com.edu.chzu.fg.smartornament.thread.ConnectedThread;
import com.edu.chzu.fg.smartornament.utils.LocationUtils;
import com.edu.chzu.fg.smartornament.view.progress_button.CircularProgressButton;

import java.util.ArrayList;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.VIBRATOR_SERVICE;
import static cn.bmob.v3.Bmob.getApplicationContext;


/**
 * Created by FG on 2017/3/25.
 */

public class LinkDeviceFragment extends Fragment{
    // 使能请求码
    private static final int REQUES_BT_ENABLE_CODE = 0x1002;
    //发送与接收到消息
    //发送与接收到消息
    public static final int TASK_SEND_MSG = 1;
    public static final int TASK_RECV_MSG = 2;
    public static final int TASK_GET_SOCKET = 3;
    public static final int TASK_GET_SOCKET_FAILED=4;
    public static final int TASK_DISCONNECT=5;
    // UUID号，表示不同的数据协议
    private final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
    private ArrayAdapter<String> adapter;          //Listview的适配器
    private ArrayList<String> mArrayList = new ArrayList<String>();    //ListView的显示内容
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();  //用于保存搜索到的蓝牙，避免重复添加
   // private Button mBtn_searchBle;   //搜索蓝牙按钮
    private CircularProgressButton mCpb_connect;
    private ImageView iv_connect;
    private BluetoothAdapter mBluetoothAdapter;    //蓝牙适配器
    private BluetoothDevice Selectdevice;          //保存连接的蓝牙地址
    private AcceptThread mAcceptThread;            //作为服务端监听线程
    private ConnectThread mRequestThread;          //作为客服端请求线程
    private ConnectedThread mangerThread;

    private LocationUtils locationUtils;
    private Vibrator vibrator;
    private AssetManager assetManager;

    private boolean isServiceStart=false;
    private MyApplication mApplication;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=LayoutInflater.from(getActivity()).inflate(R.layout.fragment_linkdevice,container,false);
        //mBtn_searchBle= (Button) view.findViewById(R.id.btn_search);
        mCpb_connect= (CircularProgressButton) view.findViewById(R.id.cpb_connect);
        iv_connect= (ImageView) view.findViewById(R.id.iv_conntect);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, mArrayList);
        locationUtils=new LocationUtils(getApplicationContext());
        vibrator = (Vibrator) getActivity(). getSystemService(VIBRATOR_SERVICE);
        mApplication= (MyApplication) getActivity().getApplication();
        mCpb_connect.setIndeterminateProgressMode(true);
        mCpb_connect.setOnClickListener(new mCpb_connectClick());
        //打开蓝牙
        openBtDevice();
        // 动态注册广播接收器
        // 用来接收扫描到的设备信息
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    //打开蓝牙，启动启动服务器端线程,蓝牙通讯运行在app全程
    private boolean openBtDevice() {
        // 获得蓝牙匹配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 蓝牙设备不被支持
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "该设备没有蓝牙设备", Toast.LENGTH_LONG).show();
            return false;
        }

        // 蓝牙如果没打开，直接提示需要打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            // 隐式Intent
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUES_BT_ENABLE_CODE);  //在onActivityResult有打开服务端线程的操作哦
        } else {
            // 如果蓝牙在运行本程序前已经人为打开，那么直接启动服务器端线程
            Toast.makeText(getActivity(), "蓝牙已经打开！	", Toast.LENGTH_LONG).show();
            //暂时不启动服务端线程，因为手机充当的总是客服端
//            mAcceptThread = new AcceptThread(mBluetoothAdapter,mHandler); // // // // // // // // // // // // // // // // // // // // // // // // //
//            mAcceptThread.start(); // // // // // // // // // // // // // // // //蓝牙提前打开就启动服务端线程？ // // // // // //
        }
        return true;
    }

    //搜索蓝牙监听事件
    private class mCpb_connectClick implements View.OnClickListener {  //目的是开启蓝牙（有可能已经开启了），最重要的是开启服务端线程

        @Override
        public void onClick(View v) {

            if (mCpb_connect.getProgress()==0||mCpb_connect.getProgress()==-1){//如果是还没连接或者是连接失败，那就重新来过呗

                // 蓝牙没有正在搜索
                if (!mBluetoothAdapter.isDiscovering()) {//如果蓝牙没在搜索，防止手残党多次点按此button
                    //服务器端线程未建立或蓝牙未开启，这个判断基本用不着，因为这线程一定开启了，所以这if绝壁不会执行
                    //////下面的逻辑执行一定会执行
                    queryPairedDevice();     //显示已配对蓝牙，首现找到已经配对的设备，再开始搜索新设备，是不是多次一举呢
                    mBluetoothAdapter.startDiscovery();  //开始搜索

                  /*准备适配器*/

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(),R.style.myCorDialog);
                    //dialog.setTitle("附近的设备");
                    View title=LayoutInflater.from(getActivity()).inflate(R.layout.notificaitn_title_linkdev,null);
                    dialog.setCustomTitle(title);

                    /**
                     * 为对话绑定适配器
                     *
                     * @param adapter:适配器，可以是Arrayadapter，SimpleAdapter等
                     * @param listener：当适配器中数据源被点时触发的方法
                     */
                    dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO: 2016/12/14  点击设备名，直接连接
                            String address = mDeviceList.get(which).getAddress();
                            Selectdevice = mBluetoothAdapter.getRemoteDevice(address);  //获得远程设备,作为服务端
                            mRequestThread = new ConnectThread(Selectdevice, mBluetoothAdapter, mHandler); // // // ////建立客户端线程 // // // // // // // // // // // // // // //
                            mRequestThread.start(); // // // // // // // //// // //客户端线程启动：选择蓝牙列表中的设备，会启动客户端线程 // // // // // //
                            mCpb_connect.setProgress(0);//如果当前是-1，不置0，圆圈转不起来。
                            mCpb_connect.setProgress(50);//开始转起来
                            // Toast.makeText(MainActivity.this, "连接" + mDeviceList.get(which).getName() + "成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.create().show();

                }
            }else {//说明已经连接成功，这里可以在对话框复用封装好之后，加个是否确定断开现有连接的提示
                //TODO 是否确定断开现有连接的提示
            }
        }
    }

    //显示已配对蓝牙
    private void queryPairedDevice() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (mDeviceList.contains(device)) {  //避免重复添加设备
                    return; //避免执行后续逻辑的好方法，记住！
                }
                mArrayList.add(device.getName() + "\n" + device.getAddress());//先把配对好的设备添加到listview的数据源中
                mDeviceList.add(device);//再添加到存放蓝牙设备的集合中，方便以后与查找到的设备比对是否在该集合中，不在就添加到这里和listview的数据源中
            }
        }
        adapter.notifyDataSetChanged();//通知适配器数据发生变化，刷新蓝牙设备列表
    }

    //广播接收器实时更新搜索到的蓝牙蓝牙列表，而广播由搜索蓝牙蓝牙时，系统自动发出广播
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated mEt_msgContenthod stub
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDeviceList.contains(device)) {  //防止重复添加蓝牙设备
                    return;
                }
                mArrayList.add(device.getName() + "\n" + device.getAddress());
                mDeviceList.add(device);
                adapter.notifyDataSetChanged();    //通知数据源更新，刷新蓝牙列表
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Toast.makeText(MainActivity.this, "搜索完成！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // 当startActivityForResult启动的 画面结束的时候，该方法被回调
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //
        if (requestCode == REQUES_BT_ENABLE_CODE && resultCode == RESULT_OK) {
            // 直接启动服务器端线程
//            mAcceptThread = new AcceptThread(mBluetoothAdapter,mHandler);
//            mAcceptThread.start(); //  //  //  //  //  //  // 蓝牙打开成功，直接启动服务端线程 //  //  //  //  //  //  //  //  //  //
            Toast.makeText(getActivity(), "蓝牙打开成功！", Toast.LENGTH_LONG).show();
        }
    }

    //Handler，负责将通信的信息更新到UI上显示，这个Handler是全局变量
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
//                case TASK_RECV_MSG://接收到对方的蓝牙信息
//                    String rcv= (String) msg.obj;  // 获得远程设备发送的消息
//                    Toast.makeText(getContext(),rcv,Toast.LENGTH_SHORT).show();
//                    if (!mApplication.getPhoneState())
//                        handleReceivedBleMsg(rcv);
//                    break;
                case TASK_GET_SOCKET:
                    mApplication.setBluetoothSocket((BluetoothSocket) msg.obj);
                    mApplication.setHandler(mHandler);
//                    mangerThread= new ConnectedThread(app.getBluetoothSocket(),getContext(),app.getHandler());
//                    mangerThread.start();
                    iv_connect.setBackgroundResource(R.drawable.connect_success);
                   // mBtn_searchBle.setText("一切准备就绪");
                    mCpb_connect.setProgress(100);
                    mRequestThread=null;//置空连接线程
                    break;
                case TASK_GET_SOCKET_FAILED:
                   // iv_connect.setBackgroundResource(R.drawable.);
                    mCpb_connect.setProgress(-1);
                    mRequestThread=null;//置空连接线程，为重新请求连接做准备
                    Toast.makeText(getActivity(), "连接失败，请重试！", Toast.LENGTH_SHORT).show();
                    break;
                case TASK_DISCONNECT:
                    mCpb_connect.setProgress(0);
                    break;
            }
        }

    };
    private void playRing() {
        try {
            MediaPlayer player=null;
            assetManager = getActivity().getAssets();
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("55555555","onDestroy");
        getActivity().unregisterReceiver(mReceiver);
    }

}




//监听蓝牙设备连接和连接断开的广播
//
//        蓝牙设备连接上和断开连接时发送, 这两个监听的是底层的连接状态
//
//        action: BluetoothDevice.ACTION_ACL_CONNECTED   BluetoothDevice.ACTION_ACL_DISCONNECTED
//

//        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
//        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//        Log.d("aaa", device.getName() + " ACTION_ACL_CONNECTED");
//        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
//        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//        Log.d("aaa", device.getName() + " ACTION_ACL_DISCONNECTED");
//        }