package com.edu.chzu.fg.smartornament.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.edu.chzu.fg.smartornament.R;
import com.edu.chzu.fg.smartornament.sql.dao.SosContacterDAO;
import com.edu.chzu.fg.smartornament.sql.model.SosContacter;
import com.edu.chzu.fg.smartornament.utils.LocationUtils;
import com.edu.chzu.fg.smartornament.utils.SosUtils;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by FG on 2017/3/6.
 */

public class SosFragment extends Fragment {
    private Context mContext;
    private LocationUtils locationUtils;
    private SosUtils sosUtils;
   /* @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;
        prefs = context.getSharedPreferences("SettingData", MODE_PRIVATE);//这玩意不能放在定义全局变量时初始化，否则没用
        preferences=context.getSharedPreferences("Location_data", MODE_PRIVATE);
        locationUtils=new LocationUtils(context);
    }*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext=activity;
        locationUtils=new LocationUtils(mContext);
        sosUtils=new SosUtils(mContext);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view=LayoutInflater.from(getActivity()).inflate(R.layout.fragment_sos,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Button btn_sos= (Button) view.findViewById(R.id.btn_sos);
        btn_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationUtils.startLocation();
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sosUtils.callForHelp();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationUtils.stopListener();
    }
}
