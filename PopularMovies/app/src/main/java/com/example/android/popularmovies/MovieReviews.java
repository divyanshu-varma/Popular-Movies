package com.example.android.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MovieReviews extends AppCompatActivity {
    private ArrayList<String> mReviewList = new ArrayList<>();
    private int mMovieId;
    private Button mRetry;
    private ProgressBar mProgessBar;
    private TextView mErrorMessage, mNoReviews;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_reviews);
        mRecyclerView = findViewById(R.id.reviews_recycler_view);
        mProgessBar = findViewById(R.id.progess_bar_reviews);
        mNoReviews = findViewById(R.id.no_reviews);
        mRetry = findViewById(R.id.retry_reviews);
        mErrorMessage = findViewById(R.id.error_message_reviews);
        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        // specify an adapter (see also next example)
        Intent intent = getIntent();
        mMovieId = intent.getIntExtra("movieId", 0);
        tryToConnect(new View(this));
    }

    private void noConnection() {
        mErrorMessage.setText(R.string.no_connection);
        mErrorMessage.setVisibility(View.VISIBLE);
        mRetry.setText(R.string.retry);
        mRetry.setVisibility(View.VISIBLE);
    }

    public void tryToConnect(View v) {
        URL movieReviewUrl = null;
        try {
            movieReviewUrl = NetworkUtils.buildUrl("reviews", mMovieId);
            new FetchMovieReviews().execute(
                    movieReviewUrl,
                    null,
                    null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (movieReviewUrl != null) {
            mErrorMessage.setVisibility(View.INVISIBLE);
            mRetry.setVisibility(View.INVISIBLE);
            new FetchMovieReviews().execute(movieReviewUrl, null, null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    private class FetchMovieReviews extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgessBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String searchResults = null;
            try {
                searchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return searchResults;
        }

        @Override
        protected void onPostExecute(String searchResults) {
            mProgessBar.setVisibility(View.INVISIBLE);
            try {
                mReviewList = new ArrayList<>();
                if (searchResults != null && !searchResults.equals("")) {
                    JSONObject jsonObject = new JSONObject(searchResults);
                    JSONArray pageOne = jsonObject.getJSONArray("results");
                    int length = pageOne.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject result = pageOne.getJSONObject(i);
                        String review = result.getString("content");
                        mReviewList.add(review);
                    }
                    if (mReviewList.isEmpty()) {
                        mNoReviews.setText(getResources().getString(R.string.no_reviews_yet));
                        mNoReviews.setVisibility(View.VISIBLE);
                    }
                    else {
                        RecyclerView.Adapter mAdapter = new MovieReviewsAdapter(mReviewList);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                } else
                    noConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

