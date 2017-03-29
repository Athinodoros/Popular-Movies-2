package com.example.athinodoros.popularmovie1.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Athinodoros on 3/26/2017.
 */

public class PopularContract {

    public static final String AUTHORITY = "com.example.athinodoros.popularmovie1";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "favorites";

    public static final class PopularEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();


        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_SYNOPSIS = "dynopis";
        public static final String COLUMN_ID = "movie_id";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_DATE = "date";

    }
}
