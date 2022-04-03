package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.popularmovies.Database.Contract;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MovieDetails extends AppCompatActivity {

    private String title;
    private long position;
    private ToggleButton favoriteButton;
    private ProgressBar mProgessBar;
    private MovieTrailersAdapter mAdapter;
    private ArrayList<String> mTrailerList = new ArrayList<>();
    private int mMovieId;
    private RecyclerView mRecyclerView;
    private TextView mErrorMessage;
    private Button mRetry;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.read_reviews, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.read_reviews) {
            Intent reviewIntent = new Intent(this, MovieReviews.class);
            reviewIntent.putExtra("movieId", mMovieId);
            startActivity(reviewIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void noConnection() {
        mErrorMessage.setText(R.string.no_connection);
        mErrorMessage.setVisibility(View.VISIBLE);
        mRetry.setText(R.string.retry);
        mRetry.setVisibility(View.VISIBLE);
    }

    public void tryToConnect(View v) {
        try {
            URL movieTrailerUrl = NetworkUtils.buildUrl("trailers", mMovieId);
            mErrorMessage.setVisibility(View.INVISIBLE);
            mRetry.setVisibility(View.INVISIBLE);
            new FetchMovieTrailers().execute(
                    movieTrailerUrl,
                    null,
                    null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        mRecyclerView = findViewById(R.id.trailer_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MovieTrailersAdapter(this, mTrailerList);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mProgessBar = findViewById(R.id.progess_bar_trailers);
        mErrorMessage = findViewById(R.id.error_message_trailers);
        mRetry = findViewById(R.id.retry_trailers);
        Intent intent = getIntent();
        mMovieId = intent.getIntExtra("movieId", 0);
        title = intent.getStringExtra("title");
        final String poster = intent.getStringExtra("poster");
        String overview = intent.getStringExtra("overview");
        String userRating = intent.getStringExtra("userRating");
        String releaseDate = intent.getStringExtra("releaseDate");
        TextView titleTV = findViewById(R.id.title);
        titleTV.setText(title);
        TextView overviewTV = findViewById(R.id.overview);
        overviewTV.setText(overview);
        TextView userRatingTV = findViewById(R.id.user_rating);
        String rating = userRating + getString(R.string.out_of_ten);
        userRatingTV.setText(rating);
        TextView releaseDateTV = findViewById(R.id.release_date);
        releaseDateTV.setText(releaseDate.substring(0, 4)); //Returns year of release
        ImageView imageView = findViewById(R.id.poster);
        String POSTER_PATH = "http://image.tmdb.org/t/p/w500/";
        Picasso.with(this).load(POSTER_PATH + poster).into(imageView);
        tryToConnect(new View(this));
        favoriteButton = findViewById(R.id.mark_as_favorite);
        boolean isFavorite = isMovieFavorite();
        if (!isFavorite)
            favoriteButton.setChecked(false);
        else
            favoriteButton.setChecked(true);


        favoriteButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        boolean isFavorite = isMovieFavorite();
                        if (!isFavorite) {

                            ContentValues contentValues = new ContentValues();

                            contentValues.put(Contract.FavoriteMovieDatabase.COLUMN_NAME_TITLE, title);
                            contentValues.put(Contract.FavoriteMovieDatabase.MOVIE_ID, mMovieId);
                            contentValues.put(Contract.FavoriteMovieDatabase.FAVORITE_POSTER_PATH, poster);

                            Uri uri = getContentResolver().insert(Contract.FavoriteMovieDatabase.CONTENT_URI, contentValues);

                            if (uri != null)
                                Toast.makeText(getBaseContext(), title + " added to favorites", Toast.LENGTH_SHORT).show();
                            favoriteButton.setChecked(true);

                        } else {

                            Uri uri = Contract.FavoriteMovieDatabase.CONTENT_URI;
                            uri = uri.buildUpon().appendPath(String.valueOf(position)).build();

                            int result = getContentResolver().delete(uri, null, null);
                            if (result > 0)
                                Toast.makeText(getBaseContext(), title + " " + "removed", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getBaseContext(), title + " " + "not a favorite", Toast.LENGTH_LONG).show();

                            favoriteButton.setChecked(false);
                        }
                    }
                }
        );
    }


    private boolean isMovieFavorite() {

        String[] projection = {"_id", "movie_id"};
        Cursor cursor = getContentResolver().query(
                Contract.FavoriteMovieDatabase.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst())
            do {
                if (cursor.getInt(cursor.getColumnIndex(Contract.FavoriteMovieDatabase.MOVIE_ID)) == mMovieId) {
                    position = cursor.getLong(cursor.getColumnIndex("_id"));
                    return true;
                }
            } while (cursor.moveToNext());

        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private class FetchMovieTrailers extends AsyncTask<URL, Void, String> {
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
                mTrailerList = new ArrayList<>();
                if (searchResults != null && !searchResults.equals("")) {
                    JSONObject jsonObject = new JSONObject(searchResults);
                    JSONArray pageOne = jsonObject.getJSONArray("results");
                    int length = pageOne.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject result = pageOne.getJSONObject(i);
                        String movieTrailerKey = result.getString("key");
                        mTrailerList.add(movieTrailerKey);
                    }
                    mAdapter = new MovieTrailersAdapter(MovieDetails.this, mTrailerList);
                    mRecyclerView.setAdapter(mAdapter);
                } else
                    noConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
