package com.edu.chzu.fg.smartornament.sql.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by FG on 2017/1/16.
 */

public class MySQLiteOpenHelper extends SQLiteOpenHelper{
    private final static String DBNAME = "smartornament.db";//定义数据库名
    private final static int VERSION = 1;   //定义数据库版本号
    public MySQLiteOpenHelper(Context context) {
        super(context, DBNAME, null, VERSION);  //重写基类的构造函数来创建数据库
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tb_sos (_id INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "name varchar(10),relation varchar(10),phone varchar(15),content varchar(10),ischecked integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,  int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {

            db.execSQL("DROP TABLE IF EXISTS tb_SosContacter");
            onCreate(db);

        }
    }





}
