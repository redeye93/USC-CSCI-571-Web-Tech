package edu.usc.placessearch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by utkar on 4/25/2018.
 */

public class ImageItemView extends RecyclerView.ViewHolder {
    ImageView image;

    public ImageItemView(View itemView) {
        super(itemView);
        this.image = itemView.findViewById(R.id.image);
    }
}
