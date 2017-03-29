package com.example.athinodoros.popularmovie1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.athinodoros.popularmovie1.data.PopularContract;
import com.example.athinodoros.popularmovie1.model.FullResponseObject;
import com.example.athinodoros.popularmovie1.model.MovieItem;
import com.example.athinodoros.popularmovie1.networking.ApiClient;
import com.example.athinodoros.popularmovie1.networking.MovieBackEndInterface;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MovieAdapter.movieClickListener, Spinner.OnItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String MOVIES_CASHED = "allMovies";
    private static final String LIST_STATE = "listState";
    private static final String SAVED_LAYOUT_MANAGER = "layout_state";
    @BindView(R.id.error_text_holder)
    TextView errorHolder;
    @BindView(R.id.movie_recycle)
    RecyclerView movieRecyclerView;
    @BindView(R.id.spinner_and_title)
    LinearLayout mSpinnerHolder;
    @BindView(R.id.sorting_spinner)
    Spinner mSpinner;

    public static final String URL_BASE = "http://api.themoviedb.org/3/movie/";
    public static final String API_KEY = ""; //TODO: A API-key is needed here
    public static final String PAGE = "page";
    public static final String ON_SAVE_INSTANSE = "callbacks";
    retrofit2.Call<FullResponseObject> call;


    private ArrayList<String> spinnerOptions = new ArrayList<>();
    public static final String ID = "ID";
    public static final String POSTER_PATH = "POSTER_PATH";
    public static final String ADULT = "ADULT";
    public static final String OVERVIEW = "OVERVIEW";
    public static final String ORIGINAL_TITLE = "ORIGINAL_TITLE";
    public static final String TITLE = "TITLE";
    public static final String R_DATE = "R_DATE";
    public static final String LANGUAGE_O = "LANGUAGE_O";
    public static final String POPULARITY = "POPULARITY";
    public static final String VOTE_COUNT = "VOTE_COUNT";
    public static final String VOTE_AVRG = "VOTE_AVRG";
    LinearLayoutManager mLayoutManager;
    MovieBackEndInterface apiService;
    SharedPreferences sharedPreferences;
    MovieAdapter movieAdapter;
    int page = 1;
    FullResponseObject moviesR;
    private Parcelable mListState;
    private String SPINNER_STATE = "spinnerState";
    private Parcelable layoutManagerSavedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(MainActivity.this);

        apiService = ApiClient.getClient().create(MovieBackEndInterface.class);
        movieAdapter = new MovieAdapter(this, this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        if (sharedPreferences.getString(getString(R.string.by), null) == null)
            sharedPreferences.edit().putString(getString(R.string.by), getString(R.string.popular_value)).commit();
        apiService = ApiClient.getClient().create(MovieBackEndInterface.class);

        spinnerOptions.add(getString(R.string.popular_value));
        spinnerOptions.add(getString(R.string.top_rated_value));
        spinnerOptions.add(getString(R.string.fav_value));
        if (mLayoutManager == null)
            mLayoutManager = new GridLayoutManager(this, calculateNoOfColumns(this), GridLayoutManager.VERTICAL, false);

        movieRecyclerView.setLayoutManager(mLayoutManager);
        movieRecyclerView.setAdapter(movieAdapter);
        movieRecyclerView.setHasFixedSize(true);
        ArrayList<MovieItem> demoData = new ArrayList<>();
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, spinnerOptions);
        mSpinner.setAdapter(stringArrayAdapter);
        String perSel = sharedPreferences.getString(getString(R.string.by), null);
        if(perSel==null){
            Toast.makeText(this, "Please select sorting again!", Toast.LENGTH_SHORT).show();
        }else if (perSel.equals(getString(R.string.popular_value))){
            mSpinner.setSelection(0);
        }else if (perSel.equals(getString(R.string.top_rated_value))){
            mSpinner.setSelection(1);
        }else if (perSel.equals(getString(R.string.fav_value))){
            mSpinner.setSelection(2);
        }
        movieAdapter.setMovieData(demoData);
        movieRecyclerView.setAdapter(movieAdapter);
        errorHolder.setVisibility(View.INVISIBLE);
        errorHolder.setText(com.example.athinodoros.popularmovie1.R.string.values_not_loaded);
        mSpinner.setOnItemSelectedListener(this);

        movieRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(getString(R.string.by), getString(R.string.popular_value)).equals(getString(R.string.fav_value))) {

                    int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                    if (lastVisibleItem > movieAdapter.getItemCount() - 5) {
                        getMovies(true);
                    }
                }
            }
        });
        if (isOnline())
            getMovies(false);
        else {
            showError();
        }

    }


    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);
        return noOfColumns;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        ApiClient.page = 0;
        if (i == 0) {
            sharedPreferences.edit().putString(getString(R.string.by), getString(R.string.popular_value)).commit();
            movieAdapter.emptyList();
            getMovies(false);
        }
        if (i == 1) {
            sharedPreferences.edit().putString(getString(R.string.by), getString(R.string.top_rated_value)).commit();
            movieAdapter.emptyList();
            getMovies(false);
        }
        if (i == 2) {
            sharedPreferences.edit().putString(getString(R.string.by), getString(R.string.fav_value)).commit();
            movieAdapter.emptyList();
            getFavorites();
            movieAdapter.notifyDataSetChanged();
        }

    }

    private void getFavorites() {
        Cursor c = getContentResolver().query(PopularContract.PopularEntry.CONTENT_URI, null, null, null, null);
        if (c.getCount() > 0) {
            ArrayList<MovieItem> data = new ArrayList<>();
            c.moveToFirst();
            while (!c.isAfterLast()) {
                MovieItem tempMovieItem = new MovieItem();
                tempMovieItem.setTitle(c.getString(c.getColumnIndex(PopularContract.PopularEntry.COLUMN_TITLE)));
                tempMovieItem.setRelease_date(c.getString(c.getColumnIndex(PopularContract.PopularEntry.COLUMN_DATE)));
                tempMovieItem.setId(c.getInt(c.getColumnIndex(PopularContract.PopularEntry.COLUMN_ID)));
                tempMovieItem.setPoster_path(c.getString(c.getColumnIndex(PopularContract.PopularEntry.COLUMN_IMAGE)));
                tempMovieItem.setVote_average(c.getFloat(c.getColumnIndex(PopularContract.PopularEntry.COLUMN_RATING)));
                tempMovieItem.setOverview(c.getString(c.getColumnIndex(PopularContract.PopularEntry.COLUMN_SYNOPSIS)));
                data.add(tempMovieItem);
                c.moveToNext();
            }
            movieAdapter.emptyList();
            movieAdapter.setMovieData(data);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void showResults() {
        errorHolder.setVisibility(View.GONE);
        mSpinnerHolder.setVisibility(View.VISIBLE);
    }

    private void showError() {
        mSpinnerHolder.setVisibility(View.GONE);
        errorHolder.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(MovieItem movie) {
        Intent intent = new Intent(this, MovieDetails.class);
        intent.putExtra(POSTER_PATH, movie.getPoster_path());
        intent.putExtra(ID, movie.getId());
        intent.putExtra(ADULT, movie.isAdult());
        intent.putExtra(OVERVIEW, movie.getOverview());
        intent.putExtra(ORIGINAL_TITLE, movie.getOriginal_title());
        intent.putExtra(TITLE, movie.getTitle());
        intent.putExtra(R_DATE, movie.getRelease_date());
        intent.putExtra(LANGUAGE_O, movie.getOriginal_language());
        intent.putExtra(POPULARITY, movie.getPopularity());
        intent.putExtra(VOTE_COUNT, movie.getVote_count());
        intent.putExtra(VOTE_AVRG, movie.getVote_average());
        onSaveInstanceState(new Bundle());
        startActivity(intent);
//        Toast.makeText(this, movieID, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shorting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void getMovies(boolean isAdding) {

        if (sharedPreferences.getString(getString(R.string.by), null).equals(getString(R.string.fav_value)) && !isAdding) {
            getFavorites();
            Log.e("FUCKKKKKKKKKKKK", "THE METHOD IS CALLED");
            movieAdapter.notifyDataSetChanged();
        } else {
            call = apiService.getMovies(sharedPreferences.getString(getString(R.string.by), getString(R.string.popular_value)), API_KEY, page);
            page++;
            call.enqueue(new Callback<FullResponseObject>() {
                @Override
                public void onResponse(retrofit2.Call<FullResponseObject> call, Response<FullResponseObject> response) {
                    if (response.body() != null)
                        if (response.body().getResults() != null) {
                            moviesR = response.body();
                            movieAdapter.setMovieData(moviesR.getResults());
                            movieAdapter.notifyDataSetChanged();
                            showResults();
                        } else
                            showError();
//                    showResults();

                }

                @Override
                public void onFailure(retrofit2.Call<FullResponseObject> call, Throwable t) {
                    showError();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        Parcelable wrapped = Parcels.wrap(FullResponseObject.class, movieAdapter.getMovieData(page));
//        outState.putParcelable(MOVIES_CASHED, wrapped);
//        mListState = mLayoutManager.onSaveInstanceState();
//        outState.putParcelable(LIST_STATE, mListState);
//        outState.putParcelable(SPINNER_STATE, mSpinner.onSaveInstanceState());
        outState.putParcelable(SAVED_LAYOUT_MANAGER, movieRecyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);

    }


    @Override
    protected void onResume() {
        super.onResume();


        movieAdapter.setMovieData(new ArrayList<MovieItem>());
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            layoutManagerSavedState = (savedInstanceState).getParcelable(SAVED_LAYOUT_MANAGER);
//            moviesR = Parcels.unwrap(savedInstanceState.getParcelable(MOVIES_CASHED));
//            movieAdapter.setMovieData(moviesR.getResults());
//
//            if (savedInstanceState.getParcelable(LIST_STATE) != null)
//                Parcels.unwrap(savedInstanceState.getParcelable(MOVIES_CASHED));
//            mListState = savedInstanceState.getParcelable(LIST_STATE);
//            mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_STATE));
//            movieRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(LIST_STATE));
////            movieRecyclerView.setLayoutManager(mLayoutManager);
////            mSpinner.onRestoreInstanceState(savedInstanceState.getParcelable(SPINNER_STATE));

        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        page = 0;
        String currentChoice = sharedPreferences.getString("BY", getString(R.string.popular_value));
        if (s.equals("BY")) {
            if (currentChoice.equals(getString(R.string.popular_value))) {
                mSpinner.setSelection(0, false);
                movieAdapter.emptyList();
                getMovies(false);
            } else if (currentChoice.equals(getString(R.string.top_rated_value))) {
                mSpinner.setSelection(1, false);
                movieAdapter.emptyList();
                getMovies(false);
            } else if (currentChoice.equals(getString(R.string.fav_value))) {
                mSpinner.setSelection(2, false);
                movieAdapter.emptyList();
                getMovies(false);

            }
        }

    }


}
