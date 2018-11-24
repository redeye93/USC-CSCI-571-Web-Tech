package edu.usc.placessearch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import org.json.JSONObject;

/**
 * Created by utkar on 4/25/2018.
 */

public class Photos extends Fragment {
    private static JSONObject specificPlace;
    RecyclerView recyclerView;

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.photos, container, false);

        if(!specificPlace.has("photos")) {
            view.findViewById(R.id.no_results_wrapper).setVisibility(ConstraintLayout.VISIBLE);
            view.findViewById(R.id.photo_list).setVisibility(RecyclerView.GONE);
            ((TextView) view.findViewById(R.id.no_results)).setText("No results");
        } else {
            try {
                recyclerView = (RecyclerView) view.findViewById(R.id.photo_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                // specify an adapter (see also next example)
                ImageList mAdapter = new ImageList(getActivity().getApplicationContext(),
                        specificPlace.getJSONArray("photos"));
                recyclerView.setAdapter(mAdapter);
            } catch(Exception e) {
                view.findViewById(R.id.no_results_wrapper).setVisibility(ConstraintLayout.VISIBLE);
                view.findViewById(R.id.photo_list).setVisibility(RecyclerView.GONE);
                ((TextView) view.findViewById(R.id.no_results)).setText("Results Error");
            }

        }

        return view;
    }

    static void setSpecificPlace(JSONObject place) {
        specificPlace = place;
    }
}
