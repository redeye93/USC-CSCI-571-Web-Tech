package edu.usc.placessearch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by utkar on 4/24/2018.
 */

public class PlaceItemView extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView placeName;
    TextView vicinity;
    ImageView utility;
    View layout;

    public PlaceItemView(View itemView) {
        super(itemView);
        layout = itemView;
        imageView = (ImageView) itemView.findViewById(R.id.icon);
        utility = (ImageView) itemView.findViewById(R.id.utility);
        vicinity = (TextView) itemView.findViewById(R.id.vicinity);
        placeName = (TextView) itemView.findViewById(R.id.name);
    }
}
