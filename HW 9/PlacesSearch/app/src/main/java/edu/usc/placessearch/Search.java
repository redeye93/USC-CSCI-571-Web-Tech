package edu.usc.placessearch;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by utkar on 4/23/2018.
 */

public class Search extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{
    private ProgressDialog dialog;
    EditText keyword;
    TextView keyword_warn;
    Spinner cat;
    EditText distance;
    RadioGroup locationChoice;
    AutoCompleteTextView otherLoc;
    TextView other_loc_warn;
    Button submit;
    Button clear;
    float longitude = Float.parseFloat("-118.2831"), latitude = Float.parseFloat("34.0266");
    View view;

    private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(33.0266, -118.2831), new LatLng(34.0266, -117.2831));

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search, container, false);

        keyword = (EditText) view.findViewById(R.id.keyword);

        keyword_warn = (TextView) view.findViewById(R.id.keyword_warn);

        cat = (Spinner) view.findViewById(R.id.category);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.category, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        cat.setAdapter(adapter);

        distance = (EditText) view.findViewById(R.id.distance);

        locationChoice = (RadioGroup) view.findViewById(R.id.location_type);

        otherLoc = (AutoCompleteTextView) view.findViewById(R.id.other_loc);

        other_loc_warn = (TextView) view.findViewById(R.id.other_loc_warn);

        submit = (Button) view.findViewById((R.id.search_button));

        clear = (Button) view.findViewById((R.id.clear_button));

        view.findViewById(R.id.other_radio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.other_loc).setEnabled(true);
            }
        });

        view.findViewById(R.id.curr_radio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.other_loc).setEnabled(false);
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear keyword
                keyword.setText("");

                keyword_warn.setVisibility(TextView.GONE);

                //Reset Category
                cat.setSelection(0);

                //Clear distance
                distance.setText("");

                //Reset Location Choice
                locationChoice.check(R.id.curr_radio);

                other_loc_warn.setVisibility(TextView.GONE);

                //Clear the other location
                otherLoc.setText("");
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()) {
                    dialog.setMessage("Fetching results");
                    dialog.show();

                    RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                    String url = "node_application/search?" + prepareQueryString();

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    dialog.dismiss();

                                    Intent results = new Intent(getActivity(), SearchResults.class);
                                    results.putExtra("results", response); //Optional parameters
                                    getActivity().startActivity(results);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(), "Session Timeout",
                                    Toast.LENGTH_SHORT).show();
                            System.out.println(error.toString());
                        }
                    });

                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);
                } else {
                    Toast t = Toast.makeText(getActivity().getApplicationContext(), R.string.errorMessage,
                            Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER | Gravity.BOTTOM,
                            0, 5);
                    t.show();
                }
            }
        });

        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String url = "http://ip-api.com/json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            latitude = Float.parseFloat(obj.getString("lat"));
                            longitude = Float.parseFloat(obj.getString("lon"));
                        } catch (Exception e) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), "GPS problem",
                        Toast.LENGTH_SHORT).show();
                System.out.println(error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        dialog = new ProgressDialog(getActivity());

        // Places autocomplete Code
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this.getActivity(), GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        otherLoc.setThreshold(3);

        otherLoc.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this.getActivity(), android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null);
        otherLoc.setAdapter(mPlaceArrayAdapter);

        return view;
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            /*CharSequence attributions = places.getAttributions();

            mNameTextView.setText(Html.fromHtml(place.getName() + ""));
            mAddressTextView.setText(Html.fromHtml(place.getAddress() + ""));
            mIdTextView.setText(Html.fromHtml(place.getId() + ""));
            mPhoneTextView.setText(Html.fromHtml(place.getPhoneNumber() + ""));
            mWebTextView.setText(place.getWebsiteUri() + "");
            if (attributions != null) {
                mAttTextView.setText(Html.fromHtml(attributions.toString()));
            }*/
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity(),
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
    }

    private boolean validate() {
        boolean flag = true;
        if(keyword.getText().toString().trim().length()==0){
            keyword_warn.setVisibility(TextView.VISIBLE);
            flag = false;
        } else {
            keyword_warn.setVisibility(TextView.GONE);
        }

        if(locationChoice.getCheckedRadioButtonId()==R.id.other_radio &&
                otherLoc.getText().toString().trim().length()==0){
            other_loc_warn.setVisibility(TextView.VISIBLE);
            flag = false;
        } else {
            other_loc_warn.setVisibility(TextView.GONE);
        }

        return flag;
    }

    private String prepareQueryString() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("keyword=").append(URLEncoder.encode(keyword.getText().toString(), "UTF-8"));
            sb.append("&category=").append(URLEncoder.encode(cat.getSelectedItemPosition()==0?"":
                    getActivity().getApplicationContext().getResources()
                            .getStringArray(R.array.category_keys)[cat.getSelectedItemPosition()], "UTF-8"));
            sb.append("&distance=").append(distance.getText().toString().trim().length()==0?10:
                    distance.getText().toString().trim());
            sb.append("&location=").append(locationChoice.getCheckedRadioButtonId()==R.id.other_radio?"other":"current");
            sb.append("&currentLocation[lat]=").append(latitude);
            sb.append("&currentLocation[lng]=").append(longitude);
            sb.append("&customLocation=").append(URLEncoder.encode(
                    otherLoc.getText().toString().trim(), "UTF-8"));

            return sb.toString();
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "URLEncoder issue: " + e.toString(),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
