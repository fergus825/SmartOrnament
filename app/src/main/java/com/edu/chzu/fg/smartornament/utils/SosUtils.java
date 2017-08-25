package com.edu.chzu.fg.smartornament.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.edu.chzu.fg.smartornament.sql.dao.SosContacterDAO;
import com.edu.chzu.fg.smartornament.sql.model.SosContacter;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by FG on 2017/4/22.
 */

public class SosUtils {
    private SharedPreferences mPreferences;
    private SosContacterDAO mSosContacterDAO;
    private SosContacter mSosContacter;
    private Context mContext;
    public SosUtils(Context context){
        mContext=context;
        mPreferences=mContext.getSharedPreferences("Location_data", MODE_PRIVATE);
        mSosContacterDAO=new SosContacterDAO(mContext);
    }
    public void callForHelp(){
        Intent intent = new Intent(Intent.ACTION_CALL);
        //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//终有用武之地
        mSosContacter=mSosContacterDAO.query();
        if(mSosContacter!=null) {
            Log.i("Test4444", "用户点击求助按钮，手机开启百度定位来获取当前位置");
            Log.i("Test6666", "用户点击求助按钮，手机自动拨打联系人电话和发送求救短信");
            Uri data = Uri.parse("tel:" + mSosContacter.getPhoneNumber());
            intent.setData(data);
            mContext.startActivity(intent);

            sendSMS(mSosContacter.getPhoneNumber(), mSosContacter.getSmsContent() + mPreferences.getString("location", "null"));
        }else {
            Toast.makeText(mContext,"请在系统设置中添加紧急联系人，并选定",Toast.LENGTH_SHORT).show();
        }
    }
    private void sendSMS(String phoneNumber,String message){
        //获取短信管理器
        SmsManager smsManager = SmsManager.getDefault();
        //拆分短信内容（手机短信长度限制）
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, null, null);
        }
    }
}
