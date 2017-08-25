package com.edu.chzu.fg.smartornament.sql.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.edu.chzu.fg.smartornament.sql.model.SosContacter;

import java.util.ArrayList;

/**
 * Created by FG on 2017/3/28.
 */

public class SosContacterDAO {
    private MySQLiteOpenHelper helper;
    private SQLiteDatabase db;
    public SosContacterDAO(Context context){
        helper=new MySQLiteOpenHelper(context);
        db=helper.getWritableDatabase();
    }
    public void insert(SosContacter sosContacter){
        db.execSQL("insert into tb_sos (_id,name,relation,phone,content) values (?,?,?,?,?)",
                new Object[]{sosContacter.getId(),sosContacter.getName(),sosContacter.getRelation(),
                sosContacter.getPhoneNumber(),sosContacter.getSmsContent()} );
    }
    public void delete(int id){
        db.execSQL("delete from tb_sos where _id =?",new Object[]{id});
    }
    public void update(int isChecked,int id){
        db.execSQL("update tb_sos set ischecked =? where _id =?",new Object[]{isChecked,id});
    }
    public SosContacter query(){
        Cursor cursor=db.rawQuery("select * from tb_sos where ischecked=1",null);
        if(cursor.moveToNext()){
            return  new SosContacter(cursor.getInt(cursor.getColumnIndex("_id")),cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("relation")),cursor.getString(cursor.getColumnIndex("phone")),
                    cursor.getString(cursor.getColumnIndex("content")),cursor.getInt(cursor.getColumnIndex("ischecked")));
        }
        return null;
    }
    public boolean queryHaveChecked(){
        Cursor cursor=db.rawQuery("select * from tb_sos where ischecked=1",null);
        return  cursor.moveToFirst();
    }
    public ArrayList<SosContacter> queryAll(){
        ArrayList<SosContacter> data=new ArrayList<SosContacter>();
        SosContacter sosContacter=null;
        Cursor cursor=db.rawQuery("select * from tb_sos",null);
        while (cursor.moveToNext()) {
           sosContacter=new SosContacter();
           sosContacter.setId(cursor.getInt(cursor.getColumnIndex("_id")));
           sosContacter.setName(cursor.getString(cursor.getColumnIndex("name")));
            sosContacter.setRelation(cursor.getString(cursor.getColumnIndex("relation")));
            sosContacter.setPhoneNumber(cursor.getString(cursor.getColumnIndex("phone")));
            sosContacter.setSmsContent(cursor.getString(cursor.getColumnIndex("content")));
            sosContacter.setIsChecked(cursor.getInt(cursor.getColumnIndex("ischecked")));
            data.add(sosContacter);
        }
        return data;
    }
    public int getMaxId(){
        Cursor cursor=db.rawQuery("select max(_id) from tb_sos",null);
        while (cursor.moveToLast()){
            return cursor.getInt(0);
        }
        return 0;
    }
}
