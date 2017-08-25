package com.edu.chzu.fg.smartornament.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.edu.chzu.fg.smartornament.R;
import com.edu.chzu.fg.smartornament.adapter.RecyclerviewAdapter;
import com.edu.chzu.fg.smartornament.sql.dao.SosContacterDAO;
import com.edu.chzu.fg.smartornament.sql.model.SosContacter;
import com.edu.chzu.fg.smartornament.utils.WrapContentLinearLayoutManager;
import com.edu.chzu.fg.smartornament.view.CustomDialog;
import com.edu.chzu.fg.smartornament.view.MyDecoration;

/**
 * Created by FG on 2017/3/30.
 */

public class SosContacterActivity extends Activity{
    private RecyclerView mRecyclerview;
    private RecyclerviewAdapter mAdapter;
    private Button btn_add;
    private EditText et_name,et_relation,et_phone,et_content;
    private ImageView iv_back;
    private SosContacterDAO mSosContacterDAO;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soscontacter);
        mRecyclerview= (RecyclerView) findViewById(R.id.recycler_view);
       // mRecyclerview.setLayoutParams();
        btn_add= (Button) findViewById(R.id.btn_add);
        et_name= (EditText) findViewById(R.id.et_name);
        et_relation= (EditText) findViewById(R.id.et_relation);
        et_phone= (EditText) findViewById(R.id.et_phone);
        et_content= (EditText) findViewById(R.id.et_content);
        iv_back= (ImageView) findViewById(R.id.image_back);
        mSosContacterDAO=new SosContacterDAO(this);
        Log.i("Test3333", "查询数据库中所有联系人数据成功");
        mAdapter=new RecyclerviewAdapter(this,mSosContacterDAO.queryAll());
      //  LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerview.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //*长按item删除*//*
        mAdapter.setOnLongClickListener(new RecyclerviewAdapter.OnLongClickListener() {
            @Override
            public void onItemClickListener(final int clickedId, String name) {
                CustomDialog.Builder builder = new CustomDialog.Builder(SosContacterActivity.this);
                builder.setTitle(R.string.prompt);
                Log.i("Test3333", "将联系人"+name+"从数据库中删除");
                builder.setMessage("确定删除紧急联系人--"+name+"？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mAdapter.remove(clickedId);
                            }
                        });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.create().show();
            }
        });
        mRecyclerview.setAdapter(mAdapter);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        //设置动画时间
        animator.setAddDuration(500);
        animator.setRemoveDuration(500);
        mRecyclerview.setItemAnimator(animator);
        mRecyclerview.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_name.getText().toString().equals("")&&!et_phone.getText().toString().equals("")){
                    SosContacter sosContacter=new SosContacter();
                    sosContacter.setId(mSosContacterDAO.getMaxId()+1);
                    sosContacter.setName(et_name.getText().toString());
                    sosContacter.setRelation(et_relation.getText().toString());
                    sosContacter.setPhoneNumber(et_phone.getText().toString());
                    sosContacter.setSmsContent(et_content.getText().toString());
                    sosContacter.setIsChecked(0);//默认被勾选为false，即0
                    mSosContacterDAO.insert(sosContacter); //添加到数据库
                    //mAdapter.addItem(sosContacter);  //将新增的数据加入到展示列表中
                    mAdapter.add(mAdapter.getDataSourceSize(),sosContacter);
                    Log.i("Test3333", "将"+et_name.getText().toString()+"作为联系人添加到数据库中");
                }else{
                    CustomDialog.Builder builder = new CustomDialog.Builder(SosContacterActivity.this);
                    builder.setTitle(R.string.prompt);
                    builder.setMessage("姓名或联系人不能为空！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            }
        });


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
