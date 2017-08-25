package com.edu.chzu.fg.smartornament.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edu.chzu.fg.smartornament.R;
import com.edu.chzu.fg.smartornament.sql.dao.SosContacterDAO;
import com.edu.chzu.fg.smartornament.sql.model.SosContacter;
import com.edu.chzu.fg.smartornament.view.CustomDialog;
import com.edu.chzu.fg.smartornament.view.SwitchButton;

import java.util.ArrayList;

/**
 * Created by FG on 2017/3/28.
 */

public class RecyclerviewAdapter extends RecyclerView.Adapter{
    private Context mContext;
    private ArrayList<SosContacter> mDataSource;
    private OnLongClickListener mLongListener;
    public static final int VIEW_TYPE_ITEM = 1;
    public static final int VIEW_TYPE_EMPTY = 0;
    public RecyclerviewAdapter(Context context, ArrayList<SosContacter> dataSource){
        mContext=context;
        mDataSource=dataSource;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i("33333","onCreateViewHolder");
        if (viewType==VIEW_TYPE_ITEM){
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview,parent,false);
            return new MyHolder(view);
        }else {
            View view1=LayoutInflater.from(mContext).inflate(R.layout.item_emptyview,parent,false);
            return new Information(view1);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Log.i("33333","onBindViewHolder");
        mDataSource=new SosContacterDAO(mContext).queryAll();
        if (holder instanceof MyHolder){
            final MyHolder myHolder= (MyHolder) holder;
            myHolder.name.setText(mDataSource.get(position).getName());
            myHolder.phone.setText(mDataSource.get(position).getPhoneNumber());
            // myHolder.isCheck.setChecked(mDataSource.get(position).getIsChecked()==1?true:false);
            if(mDataSource.get(position).getIsChecked()==1){
                myHolder.isCheck.setChecked(true);
            }else {
                myHolder.isCheck.setChecked(false);
            }
            myHolder.isCheck.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                    Log.e("777777","onCheckedChange被调用");
                    SosContacterDAO sosContacterDAO=new SosContacterDAO(mContext);
                    int id=mDataSource.get(position).getId();
                    if (sosContacterDAO.queryHaveChecked()){//如果存在被勾选的
                        if (isChecked){
                            CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
                            builder.setTitle(R.string.prompt);
                            builder.setMessage("请保证紧急联系人只能为一位")
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            myHolder.isCheck.setChecked(false);
                                            dialog.dismiss();
                                        }
                                    });
                            builder.setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int which) {
                                            myHolder.isCheck.setChecked(false);
                                            dialog.dismiss();
                                        }
                                    });
                            builder.create().show();
                        }else{
                            sosContacterDAO.update(0,id);
                            Toast.makeText(mContext,mDataSource.get(position).getName()+"被取消紧急联系人的身份",Toast.LENGTH_SHORT).show();
                        }
                    }else {//还没有被勾选的
                        if (isChecked) {
                            sosContacterDAO.update(1, id);
                            Log.i("Test3333", "成功更改联系人"+mDataSource.get(position).getName()+"的选中状态");
                            Toast.makeText(mContext, mDataSource.get(position).getName() + "被设定为紧急联系人", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });
            myHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mLongListener.onItemClickListener(position,mDataSource.get(position).getName());
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        Log.i("33333","getItemViewType");
        //在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
        if (mDataSource.size() == 0) {
            return VIEW_TYPE_EMPTY;
        }
        //如果有数据，则使用ITEM的布局
        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        Log.i("33333","getItemCount");
        if (mDataSource.size() == 0) {
            return 1;
        }
        //如果不为0，按正常的流程跑
        return mDataSource.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        private TextView name,phone;
        private SwitchButton isCheck;
        public MyHolder(View itemView) {
            super(itemView);
            name= (TextView) itemView.findViewById(R.id.txt_name);
            phone= (TextView) itemView.findViewById(R.id.txt_phone);
            isCheck= (SwitchButton)itemView.findViewById(R.id.sb_check);
        }
    }
    class Information extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView textView;
        public Information(View itemView) {
            super(itemView);
            imageView= (ImageView) itemView.findViewById(R.id.imageview);
            textView= (TextView) itemView.findViewById(R.id.txt);
        }
    }

    public int getDataSourceSize(){
        return mDataSource.size();
    }
    public void addItem(SosContacter sosContacter){
        mDataSource.add(sosContacter);

        notifyDataSetChanged();
    }
    /*带有动画的删除*/
    public void remove(int position){
        SosContacterDAO sosContacterDAO =new SosContacterDAO(mContext);
        int id= mDataSource.get(position).getId();//得到被点击的item 在数据库中的id
        sosContacterDAO.delete(id);  //根据id删除该条在数据库中的记录
        //下面将其从列表中移除
        mDataSource.remove(position);
        notifyItemRemoved(position);
        //Toast.makeText(mContext,"position为"+position+"现在大小为"+mDataSource.size(),Toast.LENGTH_SHORT).show();
        // notifyDataSetChanged();//没有这句，删除第一个，在删除剩下的第一个，但是消失的却是剩下的第二个
        notifyItemRangeChanged(position,mDataSource.size()-position);//position位置开始的itemCount个数的item是新加来的，后面的位置position要相应的更新。

    }
    /*带有动画的添加*/
    public void add(int position,SosContacter sosContacter) {
        mDataSource.add(position,sosContacter);
        notifyItemInserted(position);
        //Toast.makeText(mContext,"position为"+position+"现在大小为"+mDataSource.size(),Toast.LENGTH_SHORT).show();
        notifyItemRangeChanged(position,mDataSource.size()-position);
    }
    public interface OnLongClickListener{
        void onItemClickListener(int clickedId,String name);
    }
    //这个方法供外部调用，传入是实现接口的对象
    public void setOnLongClickListener(OnLongClickListener listener) {
        mLongListener = listener;
    }
}
