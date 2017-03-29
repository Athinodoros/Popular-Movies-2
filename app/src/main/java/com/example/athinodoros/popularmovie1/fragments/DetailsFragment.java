package com.example.athinodoros.popularmovie1.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.athinodoros.popularmovie1.R;
import com.example.athinodoros.popularmovie1.data.PopularContract;
import com.example.athinodoros.popularmovie1.data.PopularHelper;
import com.example.athinodoros.popularmovie1.model.MovieItem;
import com.example.athinodoros.popularmovie1.networking.MovieBackEndInterface;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.athinodoros.popularmovie1.data.PopularContract.PopularEntry.COLUMN_ID;
import static com.example.athinodoros.popularmovie1.data.PopularContract.PopularEntry.CONTENT_URI;


public class DetailsFragment extends Fragment {

    private static final int TASK_LOADER_ID = 0;
    private PopularHelper popularHelper;
    private final String base_img_url = "http://image.tmdb.org/t/p/w342/";
    @BindView(R.id.movie_image_holder)
    ImageView imageHolder;
    @BindView(R.id.movie_synopsis_holder)
    TextView synopsisHolder;
    @BindView(R.id.movie_rating_holder)
    RatingBar rating;
    @BindView(R.id.rel_date_holder)
    TextView dateHolder;
    @BindView(R.id.rating_in_text)
    TextView ratingTextHolder;
    Cursor c;

    MovieBackEndInterface apiService;


    @BindView(R.id.spark_button)
    SparkButton sparkButton;
    public MovieItem movieItem;
    private String where = null;

    public static DetailsFragment getInstance(MovieItem movieItem) {
        DetailsFragment fragment = new DetailsFragment();
        fragment.movieItem = movieItem;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);
        popularHelper = new PopularHelper(getActivity());




        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //getActivity().getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
        Bundle bundle = new Bundle();
        bundle.putInt("id", movieItem.getId());
        synopsisHolder.setText(movieItem.getOverview());
        Picasso.with(getContext()).load(base_img_url + movieItem.getPoster_path()).into(imageHolder);
        rating.setMax(10);
        rating.setRating(new Float(movieItem.getVote_average()).floatValue() / 2);//because there are only half the stars
        ratingTextHolder.setText("(" + movieItem.getVote_average() + ")");
        dateHolder.setText(movieItem.getRelease_date());

        //
        sparkButton.setChecked(!isFav(movieItem.getId()));
        sparkButton.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                if (buttonState) {
                    addToFavOrRemove(movieItem.getId());
                } else {

                    addToFavOrRemove(movieItem.getId());

                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        sparkButton.setEventListener(null);
    }

    private boolean isFav(int id) {
        Uri searachByIdURI = PopularContract.PopularEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        Cursor c = getActivity().getContentResolver().query(searachByIdURI, null, null, null, null);
        if (c != null)
            if (c.getCount() > 0)
                return false;
            else
                return true;
        else
           return true;
    }


    private void addToFavOrRemove(int id) {
        if (!isFav(movieItem.getId())) {
            int deletedNo = getActivity().getContentResolver().delete(CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build(), null, null);

            if (deletedNo > 0)
                Toast.makeText(getActivity(), "The movie has been removed from favorites!", Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(getActivity(), "The movie has NOT been removed from favorites!", Toast.LENGTH_SHORT).show();
            }
        } else {
            ContentValues pc = new ContentValues();
            pc.put(PopularContract.PopularEntry.COLUMN_ID, id);
            pc.put(PopularContract.PopularEntry.COLUMN_RATING, movieItem.getVote_average());
            pc.put(PopularContract.PopularEntry.COLUMN_DATE, movieItem.getRelease_date());
            pc.put(PopularContract.PopularEntry.COLUMN_IMAGE, movieItem.getPoster_path());
            pc.put(PopularContract.PopularEntry.COLUMN_SYNOPSIS, movieItem.getOverview());
            pc.put(PopularContract.PopularEntry.COLUMN_TITLE, movieItem.getTitle());
            Uri resp = getActivity().getContentResolver().insert(CONTENT_URI, pc);
            Toast.makeText(getActivity(), resp.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            movieItem = Parcels.unwrap(savedInstanceState.getParcelable(getString(R.string.movieItemParcel)));
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getString(R.string.movieItemParcel), Parcels.wrap(MovieItem.class, movieItem));
        super.onSaveInstanceState(outState);
    }
}
