package edu.usc.placessearch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by utkar on 4/25/2018.
 */

public class ReviewList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    private List<ReviewObject> data;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ReviewList(Context c, List<ReviewObject> d) {
        data = d;
        context = c;
    }

    // Bind the data to the item of list
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        try{
            // Initiate the image download and then set it as image
            if(data.get(position).getProfilePhoto()!=null || data.get(position).getProfilePhoto().trim().length()>0) {
                new DownLoadImageTask(((ReviewItemView) holder).profile).execute(data.get(position).getProfilePhoto());
            }
            ((ReviewItemView) holder).name.setText(data.get(position).getName());
            ((ReviewItemView) holder).ratingBar.setRating(data.get(position).getRating());
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ((ReviewItemView) holder).TimeStamp.setText(formatter.format(data.get(position).getCreationTime()));
            ((ReviewItemView) holder).reviewContent.setText(data.get(position).getReview());

            // Add the onclick events
            (((ReviewItemView) holder).profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = position;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(data.get(pos).getUrl()));
                    context.startActivity(i);
                }
            });
            (((ReviewItemView) holder).name).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = position;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(data.get(pos).getUrl()));
                    context.startActivity(i);
                }
            });
            (((ReviewItemView) holder).ratingBar).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = position;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(data.get(pos).getUrl()));
                    context.startActivity(i);
                }
            });
            (((ReviewItemView) holder).reviewContent).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = position;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(data.get(pos).getUrl()));
                    context.startActivity(i);
                }
            });
        } catch(Exception e) {
            Toast.makeText(context, "Error in in recycler binding",
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
        View row = inflater.inflate(R.layout.review_item, parent, false);
        ReviewItemView item = new ReviewItemView(row);
        return item;
    }

    @Override
    public int getItemCount() {
        return data.size();
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
