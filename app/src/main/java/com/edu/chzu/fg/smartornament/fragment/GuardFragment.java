package com.edu.chzu.fg.smartornament.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.edu.chzu.fg.smartornament.R;
import com.edu.chzu.fg.smartornament.activity.MainActivity;
import com.edu.chzu.fg.smartornament.application.MyApplication;
import com.edu.chzu.fg.smartornament.service.BtService;
import com.edu.chzu.fg.smartornament.view.HintPopupWindow;
import com.edu.chzu.fg.smartornament.view.RippleBackground;

import java.util.ArrayList;

/**
 * Created by FG on 2017/3/6.
 */

public class GuardFragment extends Fragment {
    private LinearLayout ll_guard;
    private RippleBackground rippleBackground;
    private Button btn_start;

    private MyApplication mMyApplication;
    private HintPopupWindow hintPopupWindow;
    private Intent intentService;
    private Intent mIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMyApplication = (MyApplication) getActivity().getApplication();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_guard, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ll_guard = (LinearLayout) view.findViewById(R.id.ll_guard);
        rippleBackground = (RippleBackground) view.findViewById(R.id.ripplebackgroud);
        final ImageView iv_protect = (ImageView) view.findViewById(R.id.iv_guard);
        btn_start = (Button) view.findViewById(R.id.btn_startService);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMyApplication.getBluetoothSocket() != null) {
                    if(!btn_start.getText().equals("点击重启服务")) {
                        intentService = new Intent();
                        intentService.setClass(getActivity(), BtService.class);
                        getActivity().startService(intentService);
                        // iv_protect.setBackgroundResource(R.drawable.protect);
                        //btn_start.setText("正在为您保驾护航");
                        ll_guard.setVisibility(View.GONE);
                        rippleBackground.setVisibility(View.VISIBLE);
                        rippleBackground.startRippleAnimation();
                    }else{//这里的重启服务只是唤醒通信线程
                        mIntent = new Intent();
                        mIntent.setAction("RESTART_SERVICE");
                        getActivity().sendBroadcast(mIntent);
                        ll_guard.setVisibility(View.GONE);
                        rippleBackground.setVisibility(View.VISIBLE);
                        rippleBackground.startRippleAnimation();
                    }
                } else {
                    Toast.makeText(getActivity(), "开启失败请先连接上智能挂饰", Toast.LENGTH_LONG).show();
                }
            }
        });
        ArrayList<String> strList = new ArrayList<>();
        strList.add("暂停服务");
        strList.add("停止服务");
        strList.add("断开连接");

        ArrayList<View.OnClickListener> clickList = new ArrayList<>();
        View.OnClickListener clickListener_pause_service = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent();
                mIntent.setAction("PAUSE_SERVICE");
                getActivity().sendBroadcast(mIntent);
                hintPopupWindow.dismissPopupWindow();
                rippleBackground.stopRippleAnimation();
                rippleBackground.setVisibility(View.GONE);
                ll_guard.setVisibility(View.VISIBLE);
                btn_start.setText("点击重启服务");
            }
        };
        View.OnClickListener clickListener_stop_service = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent();
                mIntent.setAction("STOP_SERVICE");
                mIntent.setAction("CUT_CONNECT");////////////////////////////
                getActivity().sendBroadcast(mIntent);
                hintPopupWindow.dismissPopupWindow();
                //关闭服务不仅仅会停止服务，还会关闭通信线程，为关闭app做准备
                rippleBackground.stopRippleAnimation();
                rippleBackground.setVisibility(View.GONE);
                ll_guard.setVisibility(View.VISIBLE);
                getActivity().stopService(intentService);//停止蓝牙服务
                btn_start.setText("开启侦测模式");

            }
        };
        View.OnClickListener clickListener_cut_connect = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent();
                mIntent.setAction("CUT_CONNECT");
                getActivity().sendBroadcast(mIntent);
                hintPopupWindow.dismissPopupWindow();
                //得去蓝牙设备连接界面重新去连接设备
                rippleBackground.setVisibility(View.GONE);
                ll_guard.setVisibility(View.VISIBLE);
            }
        };
        clickList.add(clickListener_pause_service);
        clickList.add(clickListener_stop_service);
        clickList.add(clickListener_cut_connect);


        hintPopupWindow = new HintPopupWindow(getActivity(), strList, clickList);
        ImageView button = (ImageView) view.findViewById(R.id.centerImage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintPopupWindow.showPopupWindow(view);
                hintPopupWindow.dismissPopupWindow();
            }
        });
    }
}
