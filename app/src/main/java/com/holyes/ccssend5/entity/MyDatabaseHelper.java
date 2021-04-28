package com.holyes.ccssend5.entity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @ClassName: MyDatabaseHelper
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:15
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {

        public static final String TAB_BILLINFOS = "CREATE TABLE IF NOT EXISTS BINFOS("
                +"allotlno text ,"
                +"saplno text ,"
                +"outstock_id text,"
                +"outstockname text ,"
                +"instock_id text ,"
                +"instockname text "
                +")";
        public MyDatabaseHelper(Context context) {
            super(context, "bills.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TAB_BILLINFOS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        public boolean tabIsExist(String tabName){
            boolean result = false;
            if(tabName == null){
                return false;
            }
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = this.getReadableDatabase();
                String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"+tabName.trim()+"' ";
                cursor = db.rawQuery(sql, null);
                if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count>0){
                        result=true;
                    }
                }

            } catch (Exception e) {
            }
            return result;
        }


    }

