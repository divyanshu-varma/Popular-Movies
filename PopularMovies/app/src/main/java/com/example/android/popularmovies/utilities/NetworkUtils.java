package com.example.android.popularmovies.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the network.
 */
public class NetworkUtils {

    private final static String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private final static String KEY = "71f505975a1ecfd3aaa40fbe63b235f3";

    public static URL buildUrl(String option) throws java.net.MalformedURLException {
        if (option.equals("top_rated"))
            return new URL(BASE_URL + "top_rated?api_key=" + KEY);
        if (option.equals("most_popular"))
            return new URL(BASE_URL + "popular?api_key=" + KEY);
        return null;
    }

    public static URL buildUrl(String option, int movieId) throws java.net.MalformedURLException {
        if (option.equals("trailers"))
            return new URL(BASE_URL + movieId + "/videos?api_key=" + KEY);
        if (option.equals("reviews"))
            return new URL(BASE_URL + movieId + "/reviews?api_key=" + KEY);
        if (option.equals("favorites"))
            return new URL(BASE_URL + movieId + "?api_key=" + KEY);
        return null;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}