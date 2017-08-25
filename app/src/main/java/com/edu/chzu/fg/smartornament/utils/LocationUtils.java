package com.edu.chzu.fg.smartornament.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by FG on 2017/3/5.
 */

public class LocationUtils {
    private LocationClient mLocationClient=null;

    private SharedPreferences prefs ;
    private SharedPreferences.Editor editor;
    private Context context;
    public LocationUtils(Context context){
        prefs = context.getSharedPreferences("Location_data", MODE_PRIVATE);//这玩意不能放在定义全局变量时初始化，否则没用
        editor=prefs.edit();
        this.context=context;
        mLocationClient=new LocationClient(context);
        initLocation();//给mLocationClient设置定位的各种参数
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=100;
        option.setScanSpan(span);//大于等于1000ms时，使用定时定位模式。调用requestLocation()后，每隔设定的时间定定一次位
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);

        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                //Receive Location
                StringBuffer sb = new StringBuffer(256);
                sb.append("定位时间: ");
                sb.append(location.getTime());
                sb.append("，纬度: ");
                sb.append(location.getLatitude());
                sb.append(",经度: ");
                sb.append(location.getLongitude());
                sb.append("，定位精度: ");
                sb.append(location.getRadius()+"米;");
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                   /*  sb.append(";我的速度");
                     sb.append(location.getSpeed());// 单位：公里每小时*/
//                     sb.append("\nsatellite : ");
//                     sb.append(location.getSatelliteNumber());
//                     sb.append("\nheight : ");
//                     sb.append(location.getAltitude());// 单位：米
//                     sb.append("\ndirection : ");
//                     sb.append(location.getDirection());// 单位度
                    sb.append("所在地址: ");
                    sb.append(location.getAddrStr());
                     sb.append("；定位方式: ");
                     sb.append("gps定位成功");

                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    sb.append("所在位置: ");
                    sb.append(location.getAddrStr());
                    //运营商信息
//                     sb.append("\noperationers : ");
//                     sb.append(location.getOperators());
//                     sb.append("；定位方式: ");
//                     sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//                     sb.append("；定位方式: ");
//                     sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
//                     sb.append("；定位方式: ");
//                     sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                   /*  sb.append("；定位方式: : ");
                     sb.append("网络不同导致定位失败，请检查网络是否通畅");*/
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//                     sb.append("；定位方式:");
//                     sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                sb.append(";位置描述: ");
                sb.append(location.getLocationDescribe());// 位置语义化信息
//                 List<Poi> list = location.getPoiList();// POI数据
//                 if (list != null) {
//                     sb.append("\npoilist size = : ");
//                     sb.append(list.size());
//                     for (Poi p : list) {
//                         sb.append("\npoi= : ");
//                         sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
//                     }
//                 }
                editor.putString("location",sb.toString()).commit();

                Log.i("111111111111",sb.toString());
//               Toast.makeText(context, prefs.getString("location",""), Toast.LENGTH_LONG).show();


            }
        });
    }
     public void startLocation(){

         if (mLocationClient!=null&&mLocationClient.isStarted()){
             mLocationClient.requestLocation();
         }else {
             mLocationClient.start();
             mLocationClient.requestLocation();
         }

     }
    public void stopListener() {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
            mLocationClient = null;
        }
    }

   /* public int requestLocation()
    发起定位，异步获取当前位置。因为是异步的，所以立即返回，不会引起阻塞。定位结果在ReceiveListener的方法OnReceive方法的参数中返回。
    需要注意：当定位SDK从定位依据判定，位置和上一次没发生变化，而且上一次定位结果可用时，则不会发起网络请求，而是返回上一次的定位结果。 返回值：
            0：正常发起了定位。
            1：服务没有启动。
            2：没有监听函数。
            6：请求间隔过短。 前后两次请求定位时间间隔不能小于1000ms。*/
}
