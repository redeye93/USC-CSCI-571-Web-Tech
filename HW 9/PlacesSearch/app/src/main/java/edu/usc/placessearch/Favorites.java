package edu.usc.placessearch;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utkar on 4/23/2018.
 */

public class Favorites extends android.support.v4.app.Fragment {
    View view;
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.place_list, container, false);

        // No navigation
        view.findViewById(R.id.navigation_wrapper).setVisibility(LinearLayout.GONE);

        populateList();

        return view;
    }

    public void populateList() {
        // Fetch Preferences
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        Gson gson = new Gson();
        String json = mPrefs.getString("favorites", "[]");
        List<PlaceItem> favs = gson.fromJson(json, new TypeToken<ArrayList<PlaceItem>>(){}.getType());

        if(favs.size()==0) {
            view.findViewById(R.id.no_results_wrapper).setVisibility(ConstraintLayout.VISIBLE);
            view.findViewById(R.id.place_list).setVisibility(RecyclerView.GONE);
            ((TextView) view.findViewById(R.id.no_results)).setText("No favorites");
        } else {
            recyclerView = (RecyclerView) view.findViewById(R.id.place_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new ListAdapter(getActivity(), getContext(), favs, this));

            recyclerView.setVisibility(RecyclerView.VISIBLE);
            view.findViewById(R.id.no_results_wrapper).setVisibility(ConstraintLayout.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        populateList();
    }
}
