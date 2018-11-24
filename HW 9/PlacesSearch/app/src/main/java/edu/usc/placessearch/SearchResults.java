package edu.usc.placessearch;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utkar on 4/24/2018.
 */

public class SearchResults extends AppCompatActivity {
    private ProgressDialog dialog;
    RecyclerView recyclerView;
    static List<PlaceItem> resultList;
    Button next;
    Button previous;
    static String nextToken = null;
    int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_list);

        // Fetch the results passed to the activity
        String json = getIntent().getStringExtra("results");
        dialog = new ProgressDialog(this);

        // Fetch the button references
        previous = findViewById(R.id.previous_button);
        next = findViewById(R.id.next_button);

        // Add click event to the previous button
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage--;
                List<PlaceItem> updated_results = new ArrayList<>();

                //Design a custom list to update to display screen
                for (int i = 20 * currentPage; i < 20 * (currentPage + 1); i++) {
                    updated_results.add(resultList.get(i));
                }

                // Update the buttons
                if (currentPage == 0) {
                    previous.setEnabled(false);
                }
                next.setEnabled(true);

                // Refresh the view
                recyclerView.setAdapter(new ListAdapter( SearchResults.this, getApplicationContext(), updated_results, null));
            }
        });

        // Add click event to the next button
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage++;

                // Check if we need to fetch the list from backend
                if (20 * currentPage >= resultList.size() && nextToken!= null) {
                    dialog.setMessage("Fetching next page");
                    dialog.show();

                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String url = "node_application_url" +
                            "nextPage?next_page_token=" + nextToken;

                    // Fetch from backend
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    dialog.dismiss();

                                    try {
                                        JSONObject nextPage = new JSONObject(response);
                                        List<PlaceItem> updated_results = PlaceItem.
                                                convertObjectList((JSONArray)
                                                nextPage.getJSONArray("results"));
                                        checkFavorites(updated_results);
                                        recyclerView.setAdapter(new ListAdapter(
                                                SearchResults.this, getApplicationContext(), updated_results, null));

                                        resultList.addAll(updated_results);

                                        // Check for next page token
                                        if (nextPage.has("next_page_token")) {
                                            nextToken = nextPage.getString("next_page_token");
                                            findViewById(R.id.next_button).setEnabled(true);
                                        } else {
                                            nextToken = null;
                                            findViewById(R.id.next_button).setEnabled(false);
                                        }
                                        findViewById(R.id.previous_button).setEnabled(true);
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(),
                                                "Next page JSON failure",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            System.out.println(error.toString());
                            Toast.makeText(getApplicationContext(), "Next page failure",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);
                } else {
                    List<PlaceItem> updated_results = new ArrayList<>();
                    for (int i = 20 * currentPage; i < 20 * (currentPage + 1) &&
                            i < resultList.size(); i++) {
                        updated_results.add(resultList.get(i));
                    }

                    findViewById(R.id.previous_button).setEnabled(true);

                    if (nextToken!=null || 20 * (currentPage + 1) <= resultList.size()) {
                        findViewById(R.id.next_button).setEnabled(true);
                    } else {
                        findViewById(R.id.next_button).setEnabled(false);
                    }

                    recyclerView.setAdapter(new ListAdapter(SearchResults.this, getApplicationContext(),
                            updated_results, null));
                }
            }
        });

        try {
            if(json==null) {
                List<PlaceItem> updateList = new ArrayList<>();

                for (int i = 20 * currentPage; i < 20 * (currentPage + 1) &&
                        i < resultList.size(); i++) {
                    updateList.add(resultList.get(i));
                }

                checkFavorites(resultList);

                recyclerView = (RecyclerView) findViewById(R.id.place_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(new ListAdapter(SearchResults.this, this, resultList, null));

                if(resultList.size()>20 || nextToken!=null){
                    findViewById(R.id.next_button).setEnabled(true);
                } else {
                    findViewById(R.id.next_button).setEnabled(false);
                }
                findViewById(R.id.previous_button).setEnabled(false);
            } else {
                JSONObject results = new JSONObject(json);

                // Check for any results
                if (results.getJSONArray("results").length() == 0) {
                    findViewById(R.id.no_results_wrapper).setVisibility(ConstraintLayout.VISIBLE);
                    findViewById(R.id.place_list).setVisibility(RecyclerView.GONE);
                    findViewById(R.id.navigation_wrapper).setVisibility(LinearLayout.GONE);
                    ((TextView) findViewById(R.id.no_results)).setText("No results");
                } else {
                    resultList = PlaceItem.convertObjectList((JSONArray) results.getJSONArray(
                            "results"));
                    checkFavorites(resultList);

                    recyclerView = (RecyclerView) findViewById(R.id.place_list);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(new ListAdapter(SearchResults.this, this, resultList, null));

                    // Check for next page token
                    if (results.has("next_page_token")) {
                        nextToken = results.getString("next_page_token");
                        findViewById(R.id.next_button).setEnabled(true);
                    } else {
                        nextToken = null;
                    }
                    findViewById(R.id.previous_button).setEnabled(false);

                    recyclerView.setVisibility(RecyclerView.VISIBLE);
                    findViewById(R.id.no_results_wrapper).setVisibility(ConstraintLayout.GONE);
                    findViewById(R.id.navigation_wrapper).setVisibility(LinearLayout.VISIBLE);
                }
            }
        } catch (Exception e) {
            findViewById(R.id.no_results_wrapper).setVisibility(ConstraintLayout.VISIBLE);
            findViewById(R.id.place_list).setVisibility(RecyclerView.GONE);
            findViewById(R.id.navigation_wrapper).setVisibility(LinearLayout.GONE);
            ((TextView) findViewById(R.id.no_results)).setText("No results");

            Toast.makeText(this.getApplicationContext(), "Error in JSON parsing",
                    Toast.LENGTH_SHORT).show();
        }


    }

    void checkFavorites(List<PlaceItem> pl) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Gson gson = new Gson();
        String json = mPrefs.getString("favorites", "[]");
        List<PlaceItem> favs = gson.fromJson(json, new TypeToken<ArrayList<PlaceItem>>(){}.getType());

        for(int i=0; i<pl.size(); i++) {
            for (int j = 0; j < favs.size(); j++) {
                if (favs.get(j).getPlaceId().equals(pl.get(i).getPlaceId())) {
                    pl.get(i).setUtility(0);
                    break;
                }
            }
        }
    }
}
