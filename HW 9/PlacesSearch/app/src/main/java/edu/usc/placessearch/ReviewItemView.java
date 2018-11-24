package edu.usc.placessearch;

import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by utkar on 4/25/2018.
 */

public class ReviewItemView extends RecyclerView.ViewHolder {
    ImageView profile;
    TextView name;
    AppCompatRatingBar ratingBar;
    TextView TimeStamp;
    TextView reviewContent;
    View layout;

    public ReviewItemView(View itemView) {
        super(itemView);
        layout = itemView;
        profile = (ImageView) itemView.findViewById(R.id.profile_pic);
        name = (TextView) itemView.findViewById(R.id.name);
        ratingBar = (AppCompatRatingBar) itemView.findViewById(R.id.rating_bar);
        TimeStamp = (TextView) itemView.findViewById(R.id.timestamp);
        reviewContent = (TextView) itemView.findViewById(R.id.review_content);
    }
}
