package com.example.xiaoxiaoouyang.sunexposure;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.xiaoxiaoouyang.sunexposure.database.UVDataDbSchema;


public class DataManager {
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public DataManager(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new UVDataBaseHelper(mContext).getWritableDatabase();
    }

    private static ContentValues getContentValues(String t, double longitude, double altitude) {
        ContentValues values = new ContentValues();
        values.put(UVDataDbSchema.UVTable.Cols.TIME, t.toString());
        values.put(UVDataDbSchema.UVTable.Cols.LONGITUDE, longitude);
        values.put(UVDataDbSchema.UVTable.Cols.ALTITUDE, altitude);
        return values;
    }

    public void addData(String time, double longitude, double altitude) {
        ContentValues contentValues = getContentValues(time, longitude, altitude);
        mDatabase.insert(UVDataDbSchema.UVTable.NAME, null, contentValues);
    }


}
