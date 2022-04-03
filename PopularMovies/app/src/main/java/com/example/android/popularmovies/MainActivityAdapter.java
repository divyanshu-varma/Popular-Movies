package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {
    private final ArrayList<String> mPosterList;
    private final Context mContext;
    private JSONArray mJsonArray;

    // Provide a suitable mConstructorInvoked (depends on the kind of dataset)
    MainActivityAdapter(Context context, ArrayList<String> posterList, JSONArray jsonArray) {
        this.mPosterList = posterList;
        this.mContext = context;
        this.mJsonArray = jsonArray;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MainActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                             int viewType) {
        // create a new view
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_image_view, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Picasso.with(mContext).load(mPosterList.get(holder.getAdapterPosition())).into(holder.mImageView);
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject jsonObject;
                    jsonObject = mJsonArray.getJSONObject(holder.getAdapterPosition());
                    Log.d("jsonarray", "." + mJsonArray.toString());
                    Intent intent = new Intent(mContext, MovieDetails.class);
                    Intent reviewIntent = new Intent(mContext, MovieReviews.class);
                    intent.putExtra("title", jsonObject.getString("title"));
                    intent.putExtra("poster", jsonObject.getString("poster_path"));
                    intent.putExtra("overview", jsonObject.getString("overview"));
                    intent.putExtra("userRating", jsonObject.getString("vote_average"));
                    intent.putExtra("releaseDate", jsonObject.getString("release_date"));
                    intent.putExtra("movieId", jsonObject.getInt("id"));
                    reviewIntent.putExtra("movieId", jsonObject.getInt("id"));
                    mContext.startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPosterList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        final ImageView mImageView;

        ViewHolder(ImageView v) {
            super(v);
            mImageView = v;
        }
    }
}