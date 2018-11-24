package edu.usc.placessearch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by utkar on 4/25/2018.
 */

public class Reviews extends Fragment {
    private static List<ReviewObject> google;

    private static List<ReviewObject> yelp;

    String curr = "google";
    int order = 0;

    private View view;
    private Spinner reviewType;
    private Spinner reviewOrder;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.reviews, container, false);

        reviewType = (Spinner) view.findViewById(R.id.reviews_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.review_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        reviewType.setAdapter(adapter);


        reviewOrder = (Spinner) view.findViewById(R.id.order_type);
        adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.review_order, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        reviewOrder.setAdapter(adapter);

        populateReviews(google);

        reviewType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(!curr.equals(reviewType.getSelectedItem().toString().toLowerCase())) {
                    if(curr.equals("yelp")){
                        populateReviews(google);
                        curr = "google";
                    } else {
                        populateReviews(yelp);
                        curr = "yelp";
                    }
                    reviewOrder.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do Nothing
            }

        });

        reviewOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position!=order) {
                    List<ReviewObject> result;
                    if(!curr.equals("yelp")){
                        result = google;
                    } else {
                        result = yelp;
                    }

                    switch(position){
                        case 0:
                            break;
                        case 1:
                            Collections.sort(result, new RRating());
                            break;
                        case 2:
                            Collections.sort(result, new Rating());
                            break;
                        case 3:
                            Collections.sort(result, new RRecent());
                            break;
                        case 4:
                            Collections.sort(result, new Recent());
                            break;
                    }
                    populateReviews(result);
                    order = position;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do Nothing
            }

        });
        return view;
    }

    public static void setSpecificPlace(List<ReviewObject> g, List<ReviewObject> y) {
        google = g;
        yelp = y;
    }

    public void populateReviews(List<ReviewObject> arg) {
        // Check for any results
        if (arg==null || arg.size()==0) {
            view.findViewById(R.id.no_review_wrapper).setVisibility(ConstraintLayout.VISIBLE);
            view.findViewById(R.id.review_list).setVisibility(RecyclerView.GONE);
            ((TextView) view.findViewById(R.id.no_reviews)).setText("No reviews");
        } else {
            try {
                recyclerView = (RecyclerView) view.findViewById(R.id.review_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                // specify an adapter (see also next example)
                ReviewList reviewAdapter = new ReviewList(getActivity().getApplicationContext(),
                        arg);
                recyclerView.setAdapter(reviewAdapter);
                recyclerView.setVisibility(RecyclerView.VISIBLE);
                view.findViewById(R.id.no_review_wrapper).setVisibility(ConstraintLayout.GONE);
            } catch(Exception e) {
                view.findViewById(R.id.no_review_wrapper).setVisibility(ConstraintLayout.VISIBLE);
                view.findViewById(R.id.review_list).setVisibility(RecyclerView.GONE);
                ((TextView) view.findViewById(R.id.no_reviews)).setText("Results error");
            }
        }
    }

    public class Rating implements Comparator<ReviewObject> {
        @Override
        public int compare(ReviewObject o1, ReviewObject o2) {
            return o1.getRating().compareTo(o2.getRating());
        }
    }

    public class RRating implements Comparator<ReviewObject> {
        @Override
        public int compare(ReviewObject o1, ReviewObject o2) {
            return o2.getRating().compareTo(o1.getRating());
        }
    }

    public class Recent implements Comparator<ReviewObject> {
        @Override
        public int compare(ReviewObject o1, ReviewObject o2) {
            return o1.getCreationTime().compareTo(o2.getCreationTime());
        }
    }

    public class RRecent implements Comparator<ReviewObject> {
        @Override
        public int compare(ReviewObject o1, ReviewObject o2) {
            return o2.getCreationTime().compareTo(o1.getCreationTime());
        }
    }
}
