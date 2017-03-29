package com.example.athinodoros.popularmovie1;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.athinodoros.popularmovie1.model.FullResponseObject;
import com.example.athinodoros.popularmovie1.model.MovieItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Athinodoros on 2/7/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private movieClickListener mClickHandler = null;
    private final Context context;
    private ArrayList<MovieItem> mMovieData = new ArrayList<>();
    private final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w342/";//probably I could have loaded different resolutions per screen ?
    private ImageView mImageView;

    public void emptyList() {
        mMovieData = new ArrayList<>();
    }


    public interface movieClickListener {
        void onClick(MovieItem movieID);
    }


    public MovieAdapter(Context context, movieClickListener mClickHandler) {
        this.context = context;
        this.mClickHandler = mClickHandler;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MovieAdapterViewHolder mMovieAdapterViewHolder = new MovieAdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false));
//        mImageView = (ImageView) parent.findViewById(R.id.image_holder);
        return mMovieAdapterViewHolder;
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        mImageView = (ImageView) holder.itemView.findViewById(R.id.image_holder);
        Picasso.with(context)
                .load(Uri.parse(POSTER_BASE_URL + mMovieData.get(position).getPoster_path()))
                .placeholder(R.mipmap.placeholder)
                .error(R.mipmap.error_icon)
                .into(mImageView);
    }

    @Override
    public int getItemCount() {
        return mMovieData.size();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mClickHandler.onClick(mMovieData.get(getLayoutPosition()));
        }
    }

    public void setMovieData(ArrayList<MovieItem> data) {
        if (data != null)
            mMovieData.addAll(data);
            notifyDataSetChanged();
    }

    public FullResponseObject getMovieData(int page) {
        FullResponseObject fullResponseObject = new FullResponseObject(page,mMovieData," "," ");
      return fullResponseObject;
    }


}
