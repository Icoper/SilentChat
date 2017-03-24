package com.example.disemk.silentchat.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by icoper on 04.02.17.
 */

public class BaseDataMaster {
    private SQLiteDatabase database;
    private BaseDataHelper dbCreator;

    private static BaseDataMaster dataMaster;

    private BaseDataMaster(Context context) {
        dbCreator = new BaseDataHelper(context);
        if (database == null || !database.isOpen()) {
            database = dbCreator.getWritableDatabase();
        }
    }

    public static BaseDataMaster getDataMaster(Context context) {
        if (dataMaster == null) {
            dataMaster = new BaseDataMaster(context);
        }
        return dataMaster;
    }

    public long insertKey(String roomKey) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BaseDataHelper.User.MAIN_KEY, roomKey);
        return database.insert(BaseDataHelper.User.TABLE_NAME, null, contentValues);
    }

    public void deleteItem(String itemKey) {
        database.delete(BaseDataHelper.User.TABLE_NAME, BaseDataHelper.User.MAIN_KEY + "='" + itemKey + "'", null);
    }

    public ArrayList<String> getKeys() {
        String query = "SELECT " + BaseDataHelper.User.MAIN_KEY + " FROM " +
                BaseDataHelper.User.TABLE_NAME;

        Cursor cursor = database.rawQuery(query, null);

        ArrayList<String> list = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(cursor.getString(0));
            cursor.moveToNext();
            Log.d("BDM", "geKeys");
        }
        ;
        return list;
    }
}
