package com.example.athinodoros.popularmovie1.fragments;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.athinodoros.popularmovie1.R;
import com.example.athinodoros.popularmovie1.model.VideosResponse;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MyvideoRecyclerViewAdapter extends RecyclerView.Adapter<MyvideoRecyclerViewAdapter.ViewHolder> {

    private final List<VideosResponse.Result> mValues;
    Context context;

    public MyvideoRecyclerViewAdapter(List<VideosResponse.Result> items, Context context) {
        mValues = items;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_video, parent, false);
        ButterKnife.bind(this, view);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.vTitle.setText(mValues.get(position).getType());
        Picasso.with(context).load("https://img.youtube.com/vi/" + holder.mItem.getKey() + "/0.jpg").into(holder.image);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAppInstalled("com.google.android.youtube")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + holder.mItem.getKey()));
                    context.startActivity(intent);
                }
            }
        });
    }


    private boolean isAppInstalled(String packageName) {
        Intent mIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getItemCount() {
        if (mValues==null) return 0;
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public final View mView;
        @BindView(R.id.video_title)
        public TextView vTitle;
        @BindView(R.id.video_image)
        ImageView image;
        public VideosResponse.Result mItem;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }

    }
}
