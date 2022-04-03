package com.example.android.popularmovies.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class FavoriteMoviesDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "FavoriteMovies.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Contract.FavoriteMovieDatabase.TABLE_NAME + " (" +
                    Contract.FavoriteMovieDatabase._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.FavoriteMovieDatabase.MOVIE_ID + " INTEGER UNIQUE NOT NULL," +
                    Contract.FavoriteMovieDatabase.FAVORITE_POSTER_PATH + " TEXT UNIQUE NOT NULL," +
                  //  Contract.FavoriteMovieDatabase.JSON_SEARCH_RESULTS + " TEXT NOT NULL," +
                    Contract.FavoriteMovieDatabase.COLUMN_NAME_TITLE + " TEXT NOT NULL);";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Contract.FavoriteMovieDatabase.TABLE_NAME;

    public FavoriteMoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

