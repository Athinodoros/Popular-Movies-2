
package com.example.athinodoros.popularmovie1.networking;

import android.content.Context;
import android.net.Uri;

import com.example.athinodoros.popularmovie1.BuildConfig;
import com.example.athinodoros.popularmovie1.MainActivity;
import com.example.athinodoros.popularmovie1.model.VideosResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Athinodoros on 2/8/2017.
 */
public final class ApiClient {

    public static final String BASE_URL = "http://api.themoviedb.org/";
    private static Retrofit retrofit = null;

    // private static final String TAG = ApiClient.class.getSimpleName();


    public static Retrofit getClient() {

        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …
// add logging as last interceptor
            httpClient.addInterceptor(logging);
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
//    public static final Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl("http://api.themoviedb.org/3/movie/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();

    public static int page = 1;


//    public static URL buildUrl(String sort) {
//
//        page++;
//        Uri builtUri = Uri.parse(sort)
//                .buildUpon().appendQueryParameter(MainActivity.API, MainActivity.API_KEY)
//                .appendQueryParameter(MainActivity.PAGE, String.valueOf(page))
//                .build();
//        URL url = null;
//        try {
//            url = new URL(builtUri.toString());
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//
//        return url;
//    }
//
//    public static String getResponseFromHttpUrl(URL url) throws IOException {
//        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//        try {
//            InputStream in = urlConnection.getInputStream();
//
//            Scanner scanner = new Scanner(in);
//            scanner.useDelimiter("\\A");
//
//            boolean hasInput = scanner.hasNext();
//            if (hasInput) {
//                return scanner.next();
//            } else {
//                return null;
//            }
//        } finally {
//            urlConnection.disconnect();
//        }
//
//    }
}