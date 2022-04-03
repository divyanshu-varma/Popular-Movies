package com.example.android.popularmovies;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.Database.Contract;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.facebook.stetho.Stetho;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private final static String BASE_POSTER_URL = "http://image.tmdb.org/t/p/w500/";
    private static String OPTION = "OPTION";
    private TextView mNoFavorites;
    private JSONArray favoriteJsonArray;
    private int optionChosen;
    private ProgressBar mProgessBar;
    private TextView mErrorMessage;
    private Button mRetry;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private URL url;
    private ArrayList<String> posterList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            optionChosen = savedInstanceState.getInt(OPTION);
        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mProgessBar = findViewById(R.id.progess_bar);
        mErrorMessage = findViewById(R.id.error_message);
        mRetry = findViewById(R.id.retry);
        mNoFavorites = findViewById(R.id.no_favorites_yet);
        tryToConnect(new View(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item.getItemId() == R.id.top_rated) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mNoFavorites.setVisibility(View.INVISIBLE);
                url = NetworkUtils.buildUrl("top_rated");
                if (url != null) {
                    mErrorMessage.setVisibility(View.INVISIBLE);
                    mRetry.setVisibility(View.INVISIBLE);
                    new FetchMovies().execute(url, null, null);
                }
                optionChosen = 1;
            }
            if (item.getItemId() == R.id.most_popular) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mNoFavorites.setVisibility(View.INVISIBLE);
                url = NetworkUtils.buildUrl("most_popular");
                if (url != null) {
                    mErrorMessage.setVisibility(View.INVISIBLE);
                    mRetry.setVisibility(View.INVISIBLE);
                    new FetchMovies().execute(url, null, null);
                }
                optionChosen = 2;
            }
            if (item.getItemId() == R.id.favorites) {
                optionFavorites();
                optionChosen = 3;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (optionChosen == 3)
            optionFavorites();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putInt(OPTION, optionChosen);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void optionFavorites() {
        try {
            Toast.makeText(this, "Loading Favorites, Please Wait", Toast.LENGTH_LONG).show();
            String[] projection = {"movie_id", "favorite_poster_path"};
            Cursor cursor = getContentResolver().query(
                    Contract.FavoriteMovieDatabase.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
            );
            ArrayList<String> posterPathArrayList = new ArrayList<>();
            ArrayList<URL> urlArrayList = new ArrayList<>();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String posterPath = cursor.getString(cursor.getColumnIndex(Contract.FavoriteMovieDatabase.FAVORITE_POSTER_PATH));
                    posterPathArrayList.add(BASE_POSTER_URL + posterPath);
                    int movieId = cursor.getInt(cursor.getColumnIndex("movie_id"));
                    URL favoriteURL = NetworkUtils.buildUrl("favorites", movieId);
                    urlArrayList.add(favoriteURL);
                } while (cursor.moveToNext());


                ArrayList<String> stringArrayList = new FetchFavorites().execute(urlArrayList, null, null).get();
                favoriteJsonArray = new JSONArray();
                for (int i = 0; i < stringArrayList.size(); i++)
                    try {
                        favoriteJsonArray.put(new JSONObject(stringArrayList.get(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                mAdapter = new MainActivityAdapter(MainActivity.this, posterPathArrayList, favoriteJsonArray);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mRecyclerView.setVisibility(View.INVISIBLE);
                mNoFavorites.setVisibility(View.VISIBLE);
            }
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void noConnection() {
        mErrorMessage.setText(R.string.no_connection);
        mErrorMessage.setVisibility(View.VISIBLE);
        mRetry.setText(R.string.retry);
        mRetry.setVisibility(View.VISIBLE);
    }

    public void tryToConnect(View v) {
        try {
            url = NetworkUtils.buildUrl("most_popular");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url != null) {
            mErrorMessage.setVisibility(View.INVISIBLE);
            mRetry.setVisibility(View.INVISIBLE);
            new FetchMovies().execute(url, null, null);
        }
    }


    private class FetchMovies extends AsyncTask<URL, Void, String> {
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
                posterList = new ArrayList<>();
                if (searchResults != null && !searchResults.equals("")) {
                    JSONObject jsonObject = new JSONObject(searchResults);
                    JSONArray pageOne = jsonObject.getJSONArray("results");
                    int length = pageOne.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject result = pageOne.getJSONObject(i);
                        String posterPath = BASE_POSTER_URL + result.getString("poster_path");
                        posterList.add(posterPath);
                    }
                    mAdapter = new MainActivityAdapter(MainActivity.this, posterList, pageOne);
                    mRecyclerView.setAdapter(mAdapter);
                } else noConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class FetchFavorites extends AsyncTask<ArrayList<URL>, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(ArrayList<URL>... params) {
            ArrayList<URL> urlArrayList = params[0];
            ArrayList<String> stringArrayList = new ArrayList<>();
            String searchResults;
            URL searchUrl;
            favoriteJsonArray = new JSONArray();
            for (int i = 0; i < urlArrayList.size(); i++)
                try {
                    searchUrl = urlArrayList.get(i);
                    searchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                    stringArrayList.add(searchResults);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return stringArrayList;
        }
    }
}
