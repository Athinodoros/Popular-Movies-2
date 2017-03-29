package com.example.athinodoros.popularmovie1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Athinodoros on 3/26/2017.
 */

public class PopularHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "popular.db";
    public static final int DATABASE_VERSION = 1;


    public PopularHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_POPULAR_TABLE = "CREATE TABLE " +
                PopularContract.PopularEntry.TABLE_NAME + "(" +
                PopularContract.PopularEntry.COLUMN_ID + " INTEGER PRIMARY KEY," +
                PopularContract.PopularEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                PopularContract.PopularEntry.COLUMN_DATE + " TEXT NOT NULL," +
                PopularContract.PopularEntry.COLUMN_IMAGE + " TEXT NOT NULL," +
                PopularContract.PopularEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL," +
                PopularContract.PopularEntry.COLUMN_RATING + " REAL NOT NULL " +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_POPULAR_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PopularContract.PopularEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
