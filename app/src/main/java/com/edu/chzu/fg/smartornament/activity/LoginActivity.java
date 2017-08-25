package com.edu.chzu.fg.smartornament.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edu.chzu.fg.smartornament.R;
import com.edu.chzu.fg.smartornament.utils.JellyInterpolator;
import com.edu.chzu.fg.smartornament.utils.UserInfo;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends Activity {
    private EditText usrName, psd;
    private TextView login,register;
    //    private CheckBox remember,auto;
    private SharedPreferences prefs ;

    private TextView mBtnLogin;
    private ImageView iv_exit;
    private View progress;

    private View mInputLayout;

    private float mWidth, mHeight;

    private LinearLayout mName, mPsw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        register = (TextView) findViewById(R.id.txt_register);
//        remember= (CheckBox) findViewById(R.id.cb_remember);
//        auto= (CheckBox) findViewById(R.id.cb_auto);
        prefs = getSharedPreferences("UserData", MODE_PRIVATE);//这玩意不能放在定义全局变量时初始化，否则没用
        //默认初始化
        Bmob.initialize(this, "3a8ad9521be563002de06fcd49efdbc4");//3a8ad9521be563002de06fcd49efdbc4
        initView();
//       if (prefs.getBoolean("remeberPsd",false)){
//
//                usrName.setText(prefs.getString("usr",""));
//                psd.setText(prefs.getString("psd",""));
//              if (prefs.getBoolean("autoLogin",false)){
//
//                    Toast.makeText(MainActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
//                    Intent intent=new Intent(this,GameActivity.class);
//                    startActivity(intent);
//                }
//        }

        register.setOnClickListener(registerListener);

        login.setOnClickListener(loginListener);

        iv_exit.setOnClickListener(exitListener);
    }

    View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            login.setText("注册");
        }
    };

    private void register() {
        final UserInfo userInfo=new UserInfo();
        final String name=usrName.getText().toString();
        final String password=psd.getText().toString();
        if (!name.equals("")&&!password.equals("")){
            BmobQuery<UserInfo> nameQuery=new BmobQuery<UserInfo>();
            nameQuery.addWhereEqualTo("name", name);
            nameQuery.setLimit(1);  //因为用户名不能重复，所以只要查询一个结果
            nameQuery.findObjects(new FindListener<UserInfo>() {
                @Override
                public void done(List<UserInfo> list, BmobException e) {
                    if (e == null) {//e为null不能说明用户一定不存在，如果不加下方的判断条件，无论存在不存在，一律都会打印出用户已经存在，
                        if (!list.get(0).getName().equals("")) {
                            Toast.makeText(LoginActivity.this, "用户已经存在", Toast.LENGTH_SHORT).show();
                        }
                    } else {//下面是查询失败的情况
                        if (e.getErrorCode() == 9015) {//用户名不存在的情况下才能注册
                            userInfo.setName(name);
                            userInfo.setPsd(password);
                            userInfo.save(new SaveListener<String>() {//将新用户的信息添加到数据库中

                                @Override
                                public void done(String objectId, BmobException e) {
                                    if (e == null) {
                                        Log.i("Test1111", "注册账号成功");
                                        Toast.makeText(LoginActivity.this, "注册成功：", Toast.LENGTH_LONG).show();
                                        login.setText("登陆");
                                    } else {
                                        if (e.getErrorCode() == 9016) {
                                            Toast.makeText(LoginActivity.this, "网络请求错误，请检查你的网络。", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                }
                            });

                        }
                        if (e.getErrorCode() == 9016) {
                            Toast.makeText(LoginActivity.this, "网络请求错误，请检查你的网络。", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }else{
            Toast.makeText(LoginActivity.this, "用户名和密码不能为空。", Toast.LENGTH_SHORT).show();
        }
    }





    View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mWidth = login.getMeasuredWidth();
            mHeight = login.getMeasuredHeight();
            if (login.getText().equals("登录")){
                mName.setVisibility(View.INVISIBLE);
                mPsw.setVisibility(View.INVISIBLE);
                inputAnimator(mInputLayout, mWidth, mHeight);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                login();
            }else{
                register();
            }
        }
    };

    private void login() {
        final String name = usrName.getText().toString();
        final String password = psd.getText().toString();
        if (!name.equals("") && !password.equals("")) {
            BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
            query.addWhereEqualTo("name", name);
            query.setLimit(1);  //因为用户名不能重复，所以只要查询一个结果
            query.findObjects(new FindListener<UserInfo>() {
                @Override
                public void done(List<UserInfo> list, BmobException e) {
                    if (e == null) {
                        if (list.get(0).getPsd().equals(password)) {
                            //先别忙着让人跳转到主界面，先得保存一下登录的记录，如是否是记住密码和自动登录，以及用户名和密码
                            SharedPreferences.Editor editor=prefs.edit();
//                            if (remember.isChecked()){//选中记住密码就得记录勾选的状态和用户的信息
                            editor.putBoolean("remeberPsd",true);
                            editor.putString("usr",name).commit();
                            editor.putString("psd",password);
                            editor.commit();
//                            }
//                            if (auto.isChecked()){
                            editor.putBoolean("autoLogin",true).commit();
//                            }
                            Log.i("Test1111", "云端登录成功");
                           // Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "用户名与密码不匹配，请检查。", Toast.LENGTH_SHORT).show();
                        }
                    } else {//下面是查询失败的情况
                        if (e.getErrorCode() == 9015) {
                            Toast.makeText(LoginActivity.this, "你输入的用户不存在。", Toast.LENGTH_SHORT).show();
                        }
                        if (e.getErrorCode() == 9016) {
                            Toast.makeText(LoginActivity.this, "网络请求错误，请检查你的网络。", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }else {
            Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
        }
    }

    View.OnClickListener  exitListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(login.getText().equals("注册")){
                login.setText("登录");
            }else {
                finish();
            }
        }
    };

    private void initView() {
        usrName = (EditText) findViewById(R.id.userName);
        psd = (EditText) findViewById(R.id.passWord);
        login = (TextView) findViewById(R.id.txt_login);
        iv_exit= (ImageView) findViewById(R.id.iv_exit);
        progress = findViewById(R.id.layout_progress);
        mInputLayout = findViewById(R.id.input_layout);
        mName = (LinearLayout) findViewById(R.id.input_layout_name);  //账号输入框
        mPsw = (LinearLayout) findViewById(R.id.input_layout_psw);  //密码输入框

    }

    private void inputAnimator(final View view, float w, float h) {

        AnimatorSet set = new AnimatorSet();

        ValueAnimator animator = ValueAnimator.ofFloat(0, w);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view
                        .getLayoutParams();
                params.leftMargin = (int) value;
                params.rightMargin = (int) value;
                view.setLayoutParams(params);
            }
        });

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mInputLayout,
                "scaleX", 1f, 0.5f);
        set.setDuration(500);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(animator, animator2);
        set.start();
        set.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                progress.setVisibility(View.VISIBLE);
                progressAnimator(progress);
                mInputLayout.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });

    }

    private void progressAnimator(final View view) {
        PropertyValuesHolder animator = PropertyValuesHolder.ofFloat("scaleX",
                0.5f, 1f);
        PropertyValuesHolder animator2 = PropertyValuesHolder.ofFloat("scaleY",
                0.5f, 1f);
        ObjectAnimator animator3 = ObjectAnimator.ofPropertyValuesHolder(view,
                animator, animator2);
        animator3.setDuration(1000);
        animator3.setInterpolator(new JellyInterpolator());
        animator3.start();

    }
}
