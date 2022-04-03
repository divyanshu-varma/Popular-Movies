package com.example.android.popularmovies.Database;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract {
    public static final String AUTHORITY = "com.example.android.popularmovies";
    // Define the possible paths for accessing data in this contract
    // This is the path for the "tasks" directory
    public static final String PATH_FAVORITES = "favorites";
    // The base content URI = "content://" + <authority>
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private Contract() {
    }

    /* Inner class that defines the table contents */
    public static class FavoriteMovieDatabase implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();
        public static final String TABLE_NAME = "favorite_movies";
        public static final String COLUMN_NAME_TITLE = "movie_name";
        public static final String MOVIE_ID = "movie_id";
        public static final String FAVORITE_POSTER_PATH = "favorite_poster_path";
        public static final String JSON_SEARCH_RESULTS = "json_search_results";

    }
}
