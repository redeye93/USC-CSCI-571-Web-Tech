package edu.usc.placessearch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRatingBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import org.json.JSONObject;

/**
 * Created by utkar on 4/25/2018.
 */

public class Info extends Fragment {
    View view;
    private static JSONObject specificPlace;

    static void setSpecificPlace(JSONObject s) {
        specificPlace = s;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.info, container, false);

        try{
            ((TextView)view.findViewById(R.id.address)).setText(specificPlace.getString("name"));
        } catch(Exception e) {
            ((TextView)view.findViewById(R.id.address)).setText("Error");
        }

        if(specificPlace.has("international_phone_number")) {
            try{
                ((TextView)view.findViewById(R.id.phoneno)).setText(
                        specificPlace.getString("international_phone_number"));
            } catch(Exception e) {
                ((TextView)view.findViewById(R.id.phoneno)).setText("0000000000");
            }
        } else {
            view.findViewById(R.id.phoneno_wrapper).setVisibility(TextView.GONE);
        }

        if(specificPlace.has("price_level")) {
            int priceLevel = 1;
            try{
                priceLevel = Integer.parseInt(specificPlace.getString("price_level"));
            } catch(Exception e) {
            }

            StringBuffer price = new StringBuffer();
            for (int i = 0; i < priceLevel; i++) {
                price.append("$");
            }
            ((TextView) view.findViewById(R.id.price)).setText(price.toString());
        } else {
            view.findViewById(R.id.price_wrapper).setVisibility(TextView.GONE);
        }

        if(specificPlace.has("rating")) {
            float rating = 1;
            try{
                rating = Float.parseFloat(specificPlace.getString("rating"));
            } catch(Exception e) {
            }

            ((AppCompatRatingBar)view.findViewById(R.id.info_rating_bar)).setRating(rating);
        } else {
            view.findViewById(R.id.rating_wrapper).setVisibility(TextView.GONE);
        }

        if(specificPlace.has("url")) {
            try{
                ((TextView)view.findViewById(R.id.google_page)).setText(
                        specificPlace.getString("url"));
            } catch(Exception e) {
                ((TextView)view.findViewById(R.id.google_page)).setText("www.google.com");
            }
        } else {
            view.findViewById(R.id.google_page_wrapper).setVisibility(TextView.GONE);
        }

        if(specificPlace.has("website")) {
            try{
                ((TextView)view.findViewById(R.id.website)).setText(
                        specificPlace.getString("website"));
            } catch(Exception e) {
                ((TextView)view.findViewById(R.id.website)).setText("www.google.com");
            }
        } else {
            view.findViewById(R.id.website_wrapper).setVisibility(TextView.GONE);
        }

        return view;
    }

}
