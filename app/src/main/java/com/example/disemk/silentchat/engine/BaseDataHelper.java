package com.example.disemk.silentchat.engine;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by icoper on 04.02.17.
 */

public class BaseDataHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "silent_chat_db";
    public static final int DB_VERSION = 1;

    public static class User implements BaseColumns {
        public static final String TABLE_NAME = " favorite_room ";
        public static final String MAIN_KEY = " room_key ";
    }

    static String SCRIPT_CREATE_TBL_MAIN = " CREATE TABLE " +
            User.TABLE_NAME + " ( " +
            User._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            User.MAIN_KEY + " TEXT" + " );";

    public BaseDataHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SCRIPT_CREATE_TBL_MAIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE " + User.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
