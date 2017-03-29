package com.example.athinodoros.popularmovie1.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.athinodoros.popularmovie1.R;
import com.example.athinodoros.popularmovie1.model.ReviewResponse;
import com.example.athinodoros.popularmovie1.model.VideosResponse;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;


public class VideoFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    RecyclerView recyclerView;

    List<VideosResponse.Result> incomingItems;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public VideoFragment() {
    }

    public static VideoFragment getInstance(List<VideosResponse.Result> incoming) {
        VideoFragment fragment = new VideoFragment();
        fragment.incomingItems = incoming;
        return fragment;
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static VideoFragment newInstance(int columnCount) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            recyclerView.setAdapter(new MyvideoRecyclerViewAdapter(incomingItems, getActivity()));

        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelable(getString(R.string.pagerState), recyclerView.getLayoutManager().onSaveInstanceState());
        VideosResponse reviewResponse = new VideosResponse();
        reviewResponse.setResults(incomingItems);
        outState.putParcelable(getString(R.string.videos), Parcels.wrap(VideosResponse.class,reviewResponse));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null){
            VideosResponse videosResponse = Parcels.unwrap(savedInstanceState.getParcelable(getString(R.string.videos)));
            incomingItems = videosResponse.getResults();
            recyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(getString(R.string.pagerState)));
            recyclerView.setAdapter(new MyvideoRecyclerViewAdapter(incomingItems,getActivity()));
            recyclerView.getAdapter().notifyDataSetChanged();
        }
        super.onViewStateRestored(savedInstanceState);
    }
}
