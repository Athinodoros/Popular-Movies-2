package com.example.athinodoros.popularmovie1.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.athinodoros.popularmovie1.R;
;
import com.example.athinodoros.popularmovie1.model.ReviewResponse;

import java.util.List;


public class MyReviewRecyclerViewAdapter extends RecyclerView.Adapter<MyReviewRecyclerViewAdapter.ViewHolder> {

    private final List<ReviewResponse.Result> mValues;

    public MyReviewRecyclerViewAdapter(List<ReviewResponse.Result> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.rAuthor.setText(mValues.get(position).getAuthor());
        holder.rComment.setText(mValues.get(position).getContent());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if (mValues==null) return 0;
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView rAuthor;
        public final TextView rComment;
        public ReviewResponse.Result mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            rAuthor = (TextView) view.findViewById(R.id.id);
            rComment = (TextView) view.findViewById(R.id.content);
        }
    }
}
