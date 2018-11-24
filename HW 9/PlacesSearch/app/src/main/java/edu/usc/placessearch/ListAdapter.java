package edu.usc.placessearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by utkar on 4/24/2018.
 */

public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    Activity activity;
    List<PlaceItem> items;
    Favorites callback;

    // Initialize the list adapter
    public ListAdapter(Activity a, Context context, List<PlaceItem> items, Favorites callback){
        this.context = context;
        this.items = items;
        this.callback = callback;
        this.activity = a;
    }

    // Bind the data to the item of list
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // Initiate the image download and then set it as image
        new DownLoadImageTask(((PlaceItemView) holder).imageView).execute(items.get(position).getIconURI());
        ((PlaceItemView) holder).placeName.setText(items.get(position).getPlaceName());
        ((PlaceItemView) holder).vicinity.setText(items.get(position).getAddress());
        if(items.get(position).getUtility()==1){
            ((PlaceItemView) holder).utility.setImageResource(R.drawable.heart_outline_black);
        } else {
            ((PlaceItemView) holder).utility.setImageResource(R.drawable.heart_fill_red);
        }

        // Add the onclick events
        (((PlaceItemView) holder).imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = position;
                launchPlaceDetails(items.get(pos), context);
            }
        });
        (((PlaceItemView) holder).placeName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = position;
                launchPlaceDetails(items.get(pos), context);
            }
        });
        (((PlaceItemView) holder).vicinity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = position;
                launchPlaceDetails(items.get(pos), context);
            }
        });

        // Utility
        (((PlaceItemView) holder).utility).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = position;
                PlaceItem i = items.get(pos);
                i.setUtility((i.getUtility()+1)%2);

                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                Gson gson = new Gson();
                String json = mPrefs.getString("favorites", "[]");
                List<PlaceItem> favs = gson.fromJson(json, new TypeToken<ArrayList<PlaceItem>>(){}.getType());

                if(items.get(position).getUtility()==1){
                    ((ImageView)v).setImageResource(R.drawable.heart_outline_black);

                    for(int j=0; j<favs.size(); j++) {
                        if(favs.get(j).getPlaceId().equals(i.getPlaceId())) {
                            favs.remove(j);
                        }
                    }

                    // Write it back
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    json = gson.toJson(favs);
                    prefsEditor.putString("favorites", json);
                    prefsEditor.commit();

                    if(callback!=null) {
                        callback.populateList();
                    }

                    Toast.makeText(context, i.getPlaceName() + " was removed from favorites",
                            Toast.LENGTH_SHORT).show();

                } else {
                    ((ImageView)v).setImageResource(R.drawable.heart_fill_red);
                    favs.add(i);

                    // Write it back
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    json = gson.toJson(favs);
                    prefsEditor.putString("favorites", json);
                    prefsEditor.commit();

                    Toast.makeText(context, i.getPlaceName() + " was added to favorites",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.place_item, parent, false);
        PlaceItemView item = new PlaceItemView(row);
        return item;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /*
        AsyncTask enables proper and easy use of the UI thread. This class
        allows to perform background operations and publish results on the UI
        thread without having to manipulate threads and/or handlers.
     */

    /*
        final AsyncTask<Params, Progress, Result>
            execute(Params... params)
                Executes the task with the specified parameters.
     */
    private class DownLoadImageTask extends AsyncTask<String,Void,Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        protected Bitmap doInBackground(String...urls){
            String urlOfImage = urls[0];
            Bitmap logo = null;
            try{
                InputStream is = new URL(urlOfImage).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){ // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(Bitmap result){
            imageView.setImageBitmap(result);
        }
    }

    private void launchPlaceDetails(PlaceItem placeItem, Context context) {
        Intent results = new Intent(activity, PlaceDetails.class);
        results.putExtra("placeobject", (new Gson()).toJson(placeItem)); //Optional parameters
        activity.startActivity(results);
    }
}
