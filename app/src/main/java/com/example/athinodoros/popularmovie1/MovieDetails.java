package com.example.athinodoros.popularmovie1;

import android.content.ClipData;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.athinodoros.popularmovie1.data.PopularContract;
import com.example.athinodoros.popularmovie1.data.PopularHelper;
import com.example.athinodoros.popularmovie1.fragments.DetailsFragment;
import com.example.athinodoros.popularmovie1.fragments.ReviewFragment;
import com.example.athinodoros.popularmovie1.fragments.VideoFragment;
import com.example.athinodoros.popularmovie1.model.MovieItem;
import com.example.athinodoros.popularmovie1.model.ReviewResponse;
import com.example.athinodoros.popularmovie1.model.VideosResponse;
import com.example.athinodoros.popularmovie1.networking.ApiClient;
import com.example.athinodoros.popularmovie1.networking.MovieBackEndInterface;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Athinodoros on 2/8/2017.
 */
public class MovieDetails extends AppCompatActivity {


    private final String base_img_url = "http://image.tmdb.org/t/p/w342/";

    private int id;
    String title;
    String path;
    String synopsis;
    float ratingIn;
    String release;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    VideosResponse videosResponse;
    ReviewResponse reviewResponse;

    private Call<VideosResponse> callVideo;
    private Call<ReviewResponse> callReviews;
    private MovieItem movieItem = new MovieItem();


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void setupViewPager(ViewPager viewPager, List<ReviewResponse.Result> reviewList, List<VideosResponse.Result> videoList, MovieItem movieItem) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(DetailsFragment.getInstance(movieItem), "Details");
        adapter.addFragment(VideoFragment.getInstance(videoList), "Trailers");
        adapter.addFragment(ReviewFragment.getInstance(reviewList), "Reviews");

        viewPager.setAdapter(adapter);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);


        //db init
//        PopularHelper popularHelper = new PopularHelper(this);
//        SQLiteDatabase mDB = popularHelper.getWritableDatabase();

        MovieBackEndInterface apiService = ApiClient.getClient().create(MovieBackEndInterface.class);
        if (getIntent() != null && getIntent().hasExtra(MainActivity.ID)) {

            id = getIntent().getExtras().getInt(MainActivity.ID);
            title = getIntent().getExtras().getString(MainActivity.TITLE);
            path = getIntent().getExtras().getString(MainActivity.POSTER_PATH);
            synopsis = getIntent().getExtras().getString(MainActivity.OVERVIEW);
            ratingIn = getIntent().getExtras().getFloat(MainActivity.VOTE_AVRG);
            release = getIntent().getExtras().getString(MainActivity.R_DATE);
        }
        movieItem.setId(id);
        movieItem.setTitle(title);
        movieItem.setPoster_path(path);
        movieItem.setOverview(synopsis);
        movieItem.setVote_average(ratingIn);
        movieItem.setRelease_date(release);
        setTitle(title);

        callVideo = apiService.getVideos(id, MainActivity.API_KEY);

        tabLayout.setupWithViewPager(viewPager);

        callReviews = apiService.getReviews(id, MainActivity.API_KEY);
        callReviews.enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                reviewResponse = response.body();
                callVideo.enqueue(new Callback<VideosResponse>() {
                    @Override
                    public void onResponse(Call<VideosResponse> call, Response<VideosResponse> response) {
                        videosResponse = response.body();
                        setupViewPager(viewPager, reviewResponse.getResults(), videosResponse.getResults(), movieItem);
                    }

                    @Override
                    public void onFailure(Call<VideosResponse> call, Throwable t) {
                        Toast.makeText(MovieDetails.this, "Get The Videos failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Toast.makeText(MovieDetails.this, "Get Reviews failed", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        viewPager.onRestoreInstanceState(savedInstanceState.getParcelable(getString(R.string.pagerState)));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putParcelable(getString(R.string.pagerState), viewPager.onSaveInstanceState());
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
