package com.example.athinodoros.popularmovie1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Athinodoros on 3/26/2017.
 */

public class Provider extends ContentProvider {

    private PopularHelper popularHelper;
    private SQLiteDatabase db;

    public static final int FAVORITES = 100;
    public static final int FAVORITE = 101;
    private static final UriMatcher sUriMatcher = buildMatcher();

    public static UriMatcher buildMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PopularContract.AUTHORITY, PopularContract.PATH_MOVIES, FAVORITES);
        uriMatcher.addURI(PopularContract.AUTHORITY, PopularContract.PATH_MOVIES + "/#", FAVORITE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        popularHelper = new PopularHelper(context);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor outCursor;
        db = popularHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FAVORITES:
                outCursor = db.query(PopularContract.PopularEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
                break;
            case FAVORITE:
                String id = uri.getPathSegments().get(1);
                String select = PopularContract.PopularEntry.COLUMN_ID+"=?";
                outCursor = db.query(PopularContract.PopularEntry.TABLE_NAME,
                        null,
                        select,
                        new String[]{id},
                        null,
                        null,
                        null);
                break;
            default:
                outCursor = null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        outCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return outCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        Uri returnedUri;

        long id = db.insert(PopularContract.PopularEntry.TABLE_NAME,null,contentValues);

        if(id>0){
            returnedUri = ContentUris.withAppendedId(PopularContract.BASE_CONTENT_URI, id);
        }else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        return returnedUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        Cursor outCursor;
        db = popularHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        String id = uri.getPathSegments().get(1);
        int res = 0;
        switch (match) {
            case FAVORITES:
                //To be implemented ? lets see...
                break;
            case FAVORITE:
                res = db.delete(PopularContract.PopularEntry.TABLE_NAME, PopularContract.PopularEntry.COLUMN_ID+"=?", new String[]{id});
                break;
        }
        if (res==0)
            throw new android.database.SQLException("Failed to delete row into " + uri);
        return res;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
