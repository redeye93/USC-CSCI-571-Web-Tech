package edu.usc.placessearch;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by utkar on 4/25/2018.
 */

public class PlaceDetails extends AppCompatActivity {

    private ProgressDialog dialog;

    private JSONObject specificPlace;

    private ImageView favIcon;

    private ImageView twitter;

    private PlaceItem placeItem;

    private List<ReviewObject> google;

    private List<ReviewObject> yelp;

    private boolean favFlag;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_details);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Fetching results");
        dialog.show();

        String obj = getIntent().getStringExtra("placeobject");
        placeItem = (new Gson()).fromJson(obj, PlaceItem.class);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(placeItem.getPlaceName());

        actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,  ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.RIGHT);
        layoutParams.rightMargin = 40;

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.action_bar_layout, null);
        v.setLayoutParams(layoutParams);
        actionBar.setCustomView(v);

        favIcon = v.findViewById(R.id.favicon);
        twitter = v.findViewById(R.id.twitter);
        favFlag = false;

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Gson gson = new Gson();
        String json = mPrefs.getString("favorites", "[]");
        final List<PlaceItem> favs = gson.fromJson(json, new TypeToken<ArrayList<PlaceItem>>(){}.getType());

        for (int j = 0; j < favs.size(); j++) {
            if (favs.get(j).getPlaceId().equals(placeItem.getPlaceId())) {
                favIcon.setImageDrawable(getDrawable(R.drawable.heart_fill_white));
                favFlag = true;
                break;
            }
        }

        // Activate onclick on the favorite icon
        favIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favFlag = !favFlag;
                placeItem.setUtility((placeItem.getUtility()+1)%2);
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Gson gson = new Gson();
                String json = mPrefs.getString("favorites", "[]");
                List<PlaceItem> favs = gson.fromJson(json, new TypeToken<ArrayList<PlaceItem>>(){}.getType());

                if(favFlag) {
                    favIcon.setImageDrawable(getDrawable(R.drawable.heart_fill_white));
                    favs.add(placeItem);

                    Toast.makeText(getApplicationContext(), placeItem.getPlaceName() + " was added to favorites",
                            Toast.LENGTH_SHORT).show();
                } else {
                    favIcon.setImageDrawable(getDrawable(R.drawable.heart_outline_white));

                    for(int j=0; j<favs.size(); j++) {
                        if(favs.get(j).getPlaceId().equals(placeItem.getPlaceId())) {
                            favs.remove(j);
                        }
                    }

                    Toast.makeText(getApplicationContext(), placeItem.getPlaceName() + " was removed from favorites",
                            Toast.LENGTH_SHORT).show();
                }

                // Write it back
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                json = gson.toJson(favs);
                prefsEditor.putString("favorites", json);
                prefsEditor.commit();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" +
                placeItem.getPlaceId() + "&key=maps_key";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            specificPlace = (new JSONObject(response)).getJSONObject("result");
                            // Create the adapter that will return a fragment for each of the three
                            // primary sections of the activity.
                            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

                            // Set up the ViewPager with the sections adapter.
                            mViewPager = (ViewPager) findViewById(R.id.place_details_container);
                            mViewPager.setAdapter(mSectionsPagerAdapter);

                            TabLayout tabLayout = (TabLayout) findViewById(R.id.place_details_tabs);

                            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                            tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

                            setupTabIcons(tabLayout);

                            // Activate onclick on the favorite icon
                            twitter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    StringBuffer sb = new StringBuffer("Check out ");
                                    sb.append(placeItem.getPlaceName() + " located at ");
                                    try{
                                        sb.append(specificPlace.getString("formatted_address"));
                                    } catch (Exception e) {
                                        sb.append("-");
                                    }
                                    sb.append(". Website: ");

                                    try{
                                        sb = new StringBuffer(URLEncoder.encode(sb.toString(), "UTF-8"));
                                    } catch (Exception e) {}

                                    sb.append("&url=");

                                    try{
                                        if(specificPlace.has("website")){
                                            sb.append(specificPlace.getString("website"));
                                        } else {
                                            sb.append(specificPlace.getString("url"));
                                        }
                                    } catch (Exception e){}

                                    String url = "https://twitter.com/intent/tweet?text=" +
                                            sb.toString() + "&hashtags=TravelAndEntertainmentSearch";

                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(url));
                                    startActivity(i);
                                }
                            });

                            if(specificPlace.has("reviews")) {
                                try{
                                    google = parseReviews(specificPlace.getJSONArray("reviews"));
                                } catch (Exception e) {
                                    google = new ArrayList<>();
                                }
                            } else {
                                google = new ArrayList<>();
                            }

                            try{
                                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                                JSONArray add_components = specificPlace.getJSONArray("address_components");
                                int add_length = add_components.length() -1;

                                String post = add_components.getJSONObject(add_length).getJSONArray("types")
                                        .getString(0).equals("postal_code")?
                                        add_components.getJSONObject(add_length).getString("short_name"):
                                        add_components.getJSONObject(--add_length).getString("short_name");
                                add_length -= 1;
                                String country = add_components.getJSONObject(add_length--).getString("short_name");
                                String state = add_components.getJSONObject(add_length--).getString("short_name");
                                String city = add_components.getJSONObject(add_length--).getString("short_name");

                                String url = "node_application/yelp?name="
                                        + URLEncoder.encode(placeItem.getPlaceName(), "UTF-8") +
                                        "&address=" + URLEncoder.encode(placeItem.getAddress(), "UTF-8") +
                                        "&city=" + URLEncoder.encode(city, "UTF-8") + "&state=" +
                                        URLEncoder.encode(state, "UTF-8") + "&country=" +
                                        URLEncoder.encode(country, "UTF-8") + "&lat=" +
                                        placeItem.getLat() + "&lng=" +
                                        placeItem.getLng() + "&post=" +
                                        post
                                        ;

                                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JSONArray res = new JSONArray(response);
                                                    yelp = parseReviews(res);

                                                } catch (Exception e) {
                                                    Toast.makeText(getApplicationContext(), "Yelp Place not found",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();

                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        dialog.dismiss();
                                        System.out.println(error.toString());
                                    }
                                });

                                // Add the request to the RequestQueue.
                                queue.add(stringRequest);
                            } catch (Exception e) {
                                yelp = null;
                                dialog.dismiss();
                            }

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Place not found",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                System.out.println(error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    private void setupTabIcons(TabLayout tabLayout) {
        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab,
                null);
        tabOne.setText("    info");
        tabOne.setAllCaps(true);
        tabOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.info_outline, 0, 0,
                0);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab,
                null);
        tabTwo.setText("    photos ");
        tabTwo.setAllCaps(true);
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.photos, 0, 0, 0);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab,
                null);
        tabThree.setText("    map");
        tabThree.setAllCaps(true);
        tabThree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.maps, 0, 0, 0);
        tabLayout.getTabAt(2).setCustomView(tabThree);

        TextView tabFour = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab,
                null);
        tabFour.setText("    reviews");
        tabFour.setAllCaps(true);
        tabFour.setCompoundDrawablesWithIntrinsicBounds(R.drawable.review, 0, 0, 0);
        tabLayout.getTabAt(3).setCustomView(tabFour);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    Info.setSpecificPlace(specificPlace);
                    Info search = new Info();
                    return search;
                case 1:
                    Photos.setSpecificPlace(specificPlace);
                    Photos photos = new Photos();
                    return photos;
                case 2:
                    Map.setSpecificPlace(specificPlace);
                    Map map = new Map();
                    return map;
                case 3:
                    Reviews.setSpecificPlace(google, yelp);
                    Reviews reviews = new Reviews();
                    return reviews;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private List<ReviewObject> parseReviews(JSONArray json) {
        List<ReviewObject> results = new ArrayList<>();

        if(json.length()>0) {
            for(int i=0; i<json.length(); i++) {
                ReviewObject obj = new ReviewObject();
                results.add(obj);

                try {
                    if(json.getJSONObject(i).has("author_name")) {
                        obj.setName(json.getJSONObject(i).getString("author_name"));
                    }

                    if(json.getJSONObject(i).has("author_url")) {
                        obj.setUrl(json.getJSONObject(i).getString("author_url"));
                    }

                    if(json.getJSONObject(i).has("profile_photo_url")) {
                        obj.setProfilePhoto(json.getJSONObject(i).getString("profile_photo_url"));
                    }

                    if(json.getJSONObject(i).has("rating")) {
                        obj.setRating((float)json.getJSONObject(i).getDouble("rating"));
                    }

                    if(json.getJSONObject(i).has("text")) {
                        obj.setReview(json.getJSONObject(i).getString("text"));
                    }

                    if(json.getJSONObject(i).has("time")) {
                        obj.setCreationTime(new Date(Long.parseLong(json.getJSONObject(i).getString("time"))*1000));
                    }
                } catch(Exception e) {

                }

            }
        }

        return results;
    }
}
