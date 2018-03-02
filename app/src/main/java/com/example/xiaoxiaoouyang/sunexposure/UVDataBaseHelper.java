package com.example.xiaoxiaoouyang.sunexposure;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.xiaoxiaoouyang.sunexposure.database.UVDataDbSchema;
import com.example.xiaoxiaoouyang.sunexposure.database.UVDataDbSchema.UVTable;

public class UVDataBaseHelper extends SQLiteOpenHelper{
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "UVDataBase.db";

    public UVDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db){
        db.execSQL("create table " + UVTable.NAME + "(" +
        "_id integer primary key autoincrement, " +
        UVTable.Cols.TIME + ", " + UVTable.Cols.LONGITUDE + ", " + UVTable.Cols.ALTITUDE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
