package com.edu.chzu.fg.smartornament.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.edu.chzu.fg.smartornament.R;
import com.edu.chzu.fg.smartornament.activity.SosContacterActivity;
import com.edu.chzu.fg.smartornament.application.MyApplication;
import com.edu.chzu.fg.smartornament.utils.LocationUtils;
import com.edu.chzu.fg.smartornament.view.CustomDialog;
import com.edu.chzu.fg.smartornament.view.SwitchButton;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by FG on 2017/3/6.
 */

public class SettingFragment extends Fragment{
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        prefs = getActivity().getSharedPreferences("SettingData", MODE_PRIVATE);//这玩意不能放在定义全局变量时初始化，否则没用
//        preferences=getContext().getSharedPreferences("Location_data", MODE_PRIVATE);
        editor=prefs.edit();
        View view=LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TextView txt_contactNum,txt_tellOthers;
        final SwitchButton sb_gesture,sb_position,sb_shake,sb_fingerprint;
        txt_contactNum= (TextView) view.findViewById(R.id.txt_contactNum);
        sb_gesture= (SwitchButton) view.findViewById(R.id.sb_gesture);
        sb_gesture.setChecked(prefs.getBoolean("gesture",true));
        txt_tellOthers= (TextView) view.findViewById(R.id.txt_tellOthers);



        txt_contactNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), SosContacterActivity.class);
                startActivity(intent);
            }
        });

        sb_gesture.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final SwitchButton view, final boolean isChecked) {
                Log.e("777777","onCheckedChange被调用");
                if (isChecked){
                    editor.putBoolean("gesture",true);
                    final CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
                    builder.setTitle(R.string.prompt);
                    builder.setMessage("已经开启铡刀菜单快捷开关,空白处左滑动打开,右滑关闭。");
                    builder.setPositiveButton(R.string.confirm, dialogButtonClickListener);
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            view.setChecked(false);
                            editor.putBoolean("gesture",false);
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }else{
                    editor.putBoolean("gesture",false);
                    CustomDialog.Builder builder1 = new CustomDialog.Builder(getActivity());
                    builder1.setTitle(R.string.prompt);
                    builder1.setMessage("确定关闭铡刀菜单快捷开关?");
                    builder1.setPositiveButton(R.string.confirm,dialogButtonClickListener);
                    builder1.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            view.setChecked(true);
                            editor.putBoolean("gesture",true);
                            dialog.dismiss();
                        }
                    });
                    builder1.create().show();
                }
                editor.commit();
            }
        });

        txt_tellOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "给各位伙伴们安利一个神器——智能挂饰app");
                shareIntent.setType("text/plain");
                //设置分享列表的标题，并且每次都显示分享列表
                startActivity(Intent.createChooser(shareIntent, "分享到"));
            }
        });

//
//        LocationUtils locationUtils=new LocationUtils(getContext());
//        locationUtils.startLocation();
//        Toast.makeText(getContext(), preferences.getString("location",""), Toast.LENGTH_SHORT).show();

    }
    private DialogInterface.OnClickListener dialogButtonClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
        }
    };

}
