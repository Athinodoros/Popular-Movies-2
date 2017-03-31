package com.example.athinodoros.popularmovie1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
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
    private Parcelable recyclerViewState;
    private static Bundle mBundleRecyclerViewState;
    private static final String MOVIES_CASHED = "allMovies";
    private static final String LIST_STATE = "listState";
    private static final String SAVED_LAYOUT_MANAGER = "layout_state";
    private static final String PAGE_INDEX = "page";
    private static final String LAST_POSITION = "position";
    private static final String LIST_STATE_KEY = "state_key";
    @BindView(R.id.error_text_holder)
    TextView errorHolder;
    @BindView(R.id.movie_recycle)
    RecyclerView movieRecyclerView;
    @BindView(R.id.spinner_and_title)
    LinearLayout mSpinnerHolder;
    @BindView(R.id.sorting_spinner)
    Spinner mSpinner;

    public static final String API_KEY = "32439a4da71be2c154731e20277cf56d"; //TODO: A API-key is needed here

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
    GridLayoutManager mLayoutManager;
    MovieBackEndInterface apiService;
    SharedPreferences sharedPreferences;
    MovieAdapter movieAdapter;
    int page = -1;
    FullResponseObject moviesR;
    private Parcelable mListState;
    private String SPINNER_STATE = "spinnerState";
    private Parcelable layoutManagerSavedState;
    int position = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(MainActivity.this);

        apiService = ApiClient.getClient().create(MovieBackEndInterface.class);
        if (movieAdapter == null)
            movieAdapter = new MovieAdapter(this, this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        if (sharedPreferences.getString(getString(R.string.by), null) == null)
            sharedPreferences.edit().putString(getString(R.string.by), getString(R.string.popular_value)).commit();
        apiService = ApiClient.getClient().create(MovieBackEndInterface.class);

        spinnerOptions.add(getString(R.string.popular_value));
        spinnerOptions.add(getString(R.string.top_rated_value));
        spinnerOptions.add(getString(R.string.fav_value));

        mLayoutManager = new GridLayoutManager(this, calculateNoOfColumns(this), GridLayoutManager.VERTICAL, false);

        movieRecyclerView.setLayoutManager(mLayoutManager);
        movieRecyclerView.setAdapter(movieAdapter);
        movieRecyclerView.setHasFixedSize(true);
        ArrayList<MovieItem> demoData = demoData = new ArrayList<>();
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, spinnerOptions);
        mSpinner.setAdapter(stringArrayAdapter);
        String perSel = sharedPreferences.getString(getString(R.string.by), null);
        if (perSel == null) {
            Toast.makeText(this, "Please select sorting again!", Toast.LENGTH_SHORT).show();
        } else if (perSel.equals(getString(R.string.popular_value))) {
            mSpinner.setSelection(0);
        } else if (perSel.equals(getString(R.string.top_rated_value))) {
            mSpinner.setSelection(1);
        } else if (perSel.equals(getString(R.string.fav_value))) {
            mSpinner.setSelection(2);
        }
        movieAdapter.setMovieData(demoData);
        movieRecyclerView.setAdapter(movieAdapter);
        errorHolder.setVisibility(View.INVISIBLE);
        errorHolder.setText(com.example.athinodoros.popularmovie1.R.string.values_not_loaded);
        mSpinner.setOnItemSelectedListener(this);
        movieRecyclerView.addOnScrollListener(mScrollListener);
        if (savedInstanceState != null) {
            mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_STATE));
            movieRecyclerView.getLayoutManager().scrollToPosition(savedInstanceState.getInt(LAST_POSITION));
        }

        if (isOnline())
            getMovies(false);
        else {
            showError();
        }
        if (savedInstanceState != null)
            position = savedInstanceState.getInt(LAST_POSITION);
        if (mBundleRecyclerViewState != null)
            movieRecyclerView.getLayoutManager().onRestoreInstanceState(mBundleRecyclerViewState.getParcelable(LIST_STATE));
        movieRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBundleRecyclerViewState != null)
                    movieRecyclerView.scrollToPosition(mBundleRecyclerViewState.getInt(LAST_POSITION));

            }
        }, 1500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String by = sharedPreferences.getString(getString(R.string.by), null);
        if (by != null && by.equals(getString(R.string.fav_value))) {
            movieAdapter.emptyList();
            getFavorites();
            movieAdapter.notifyDataSetChanged();
        }
        if (mBundleRecyclerViewState != null) {

            Parcelable listState = mBundleRecyclerViewState.getParcelable(LIST_STATE);
            movieRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
        //restoreRecycler();
    }


    @Override
    protected void onPause() {
        super.onPause();
        position = mLayoutManager.findFirstVisibleItemPosition();
        moviesR = movieAdapter.getMovieData(0);
        mBundleRecyclerViewState = new Bundle();
        mListState = movieRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(LIST_STATE, mListState);
        mBundleRecyclerViewState.putInt(LAST_POSITION, position);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (movieAdapter.getItemCount() == 0)
            movieAdapter.setMovieData(moviesR.getResults());
        movieAdapter.notifyDataSetChanged();
        movieRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                movieRecyclerView.scrollToPosition(position);

            }
        }, 900);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (movieAdapter.getItemCount() == 0 && moviesR != null)
            movieAdapter.setMovieData(moviesR.getResults());
        movieAdapter.notifyDataSetChanged();
        movieRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                movieRecyclerView.scrollToPosition(position);

            }
        }, 900);
    }

    RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(getString(R.string.by), getString(R.string.popular_value)).equals(getString(R.string.fav_value))) {
            } else {

                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                if (lastVisibleItem > movieAdapter.getItemCount() - 5) {
                    getMovies(true);
                }
            }
        }
    };

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

        startActivity(intent);
//        Toast.makeText(this, movieID, Toast.LENGTH_SHORT).show();
    }


    public void restoreRecycler() {
        if (mBundleRecyclerViewState != null) {
            movieRecyclerView.getLayoutManager().onRestoreInstanceState(mBundleRecyclerViewState.getParcelable(LIST_STATE));
            movieRecyclerView.scrollToPosition(position);
            Log.d("restored index", String.valueOf(mLayoutManager.findFirstVisibleItemPosition()));
        }
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
            if (movieRecyclerView.getLayoutManager().getItemCount() > 0)
                page = movieRecyclerView.getLayoutManager().getItemCount() / 20 + 1;
            else
                page = 1;
            call = apiService.getMovies(sharedPreferences.getString(getString(R.string.by), getString(R.string.popular_value)), API_KEY, page);
            if (!call.isExecuted())
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
                    }

                    @Override
                    public void onFailure(retrofit2.Call<FullResponseObject> call, Throwable t) {
                        showError();
                    }
                });
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        page = 0;
        String currentChoice = sharedPreferences.getString(getString(R.string.by), getString(R.string.popular_value));
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LIST_STATE, mLayoutManager.onSaveInstanceState());
        outState.putInt(LAST_POSITION, position);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.e("CalledLeme", "POAK");
        mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_STATE));
        position = savedInstanceState.getInt(LAST_POSITION);


        movieAdapter.notifyDataSetChanged();
        movieRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                movieRecyclerView.scrollToPosition(position);
//                movieRecyclerView.smoothScrollToPosition(movieRecyclerView,null,position);
            }
        }, 1000);
//        mLayoutManager.scrollToPosition(position);
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        call.cancel();
    }


}


