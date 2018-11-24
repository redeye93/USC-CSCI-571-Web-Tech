package edu.usc.placessearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by utkar on 4/25/2018.
 */

public class ImageList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    private JSONArray data;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ImageList(Context c, JSONArray d) {
        data = d;
        context = c;
    }

    // Bind the data to the item of list
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        try{
            String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&" +
                    "photoreference=" + data.getJSONObject(position).getString("photo_reference")
                    + "&key=maps_key";

            new DownLoadImageTask(((ImageItemView) holder).image).execute(url);
        } catch (Exception e) {
            Toast.makeText(context, "Error in in image fetching",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.photo_item, parent, false);

        ImageItemView item = new ImageItemView(row);
        return item;
    }

    @Override
    public int getItemCount() {
        return data.length();
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
}
