package edu.usc.placessearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by utkar on 4/25/2018.
 */

public class Map extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback {
    private static JSONObject specificPlace;
    Spinner modeType;
    AutoCompleteTextView from;
    View view;
    String destPlaceId = null;
    LatLng originLocation;
    GoogleMap googleMap;
    Polyline polyline = null;
    Marker destMarker;

    private static GoogleApiClient mGoogleApiClient = null;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(33.0266, -118.2831), new LatLng(34.0266, -117.2831));

    static void setSpecificPlace(JSONObject s) {
        specificPlace = s;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.maps, container, false);

        modeType = (Spinner) view.findViewById(R.id.mode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.mode_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        modeType.setAdapter(adapter);

        modeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(destPlaceId!=null) {
                    updateMapRoute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do Nothing
            }

        });

        from = view.findViewById(R.id.from);

        // Places autocomplete Code
        if (mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Places.GEO_DATA_API)
                    .enableAutoManage(this.getActivity(), GOOGLE_API_CLIENT_ID, this)
                    .addConnectionCallbacks(this)
                    .build();
            from.setThreshold(3);
        }

        from.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this.getActivity(),
                android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null);
        from.setAdapter(mPlaceArrayAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap gm) {
        // Add a marker in Location
        // and move the map's camera to the same location.

        googleMap = gm;
        try{
            originLocation = new LatLng(Double.parseDouble(specificPlace.getJSONObject("geometry").
                    getJSONObject("location").getString("lat")),
                    Double.parseDouble(specificPlace.getJSONObject("geometry").
                    getJSONObject("location").getString("lng")));
            googleMap.addMarker(new MarkerOptions().position(originLocation)
                    .title(specificPlace.getString("name")));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLocation,15));
        } catch(Exception e) {
            LatLng location = new LatLng(37.398160, -122.180831);
            googleMap.addMarker(new MarkerOptions().
                    position(location).title("Mountain View"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        }
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
            destPlaceId = place.getId();
            updateMapRoute();
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

    private void updateMapRoute() {
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                originLocation.latitude + "," + originLocation.longitude
                +"&destination=place_id:" + destPlaceId + "&mode=" + modeType.getSelectedItem().toString().toLowerCase() +
                "&key=map_key";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray json = (new JSONObject(response))
                                    .getJSONArray("routes").getJSONObject(0)
                                    .getJSONArray("legs").getJSONObject(0)
                                    .getJSONArray("steps");

                            LatLng origin = new LatLng(json.getJSONObject(0)
                                    .getJSONObject("start_location").getDouble("lat"),
                                    json.getJSONObject(0)
                                            .getJSONObject("start_location")
                                            .getDouble("lng"));
                            // Create a polynomial path from start location
                            PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.add(origin);

                            LatLng endMarker = origin;
                            for(int i=0; i<json.length(); i++) {
                                endMarker = new LatLng(json.getJSONObject(i)
                                        .getJSONObject("end_location").getDouble("lat"),
                                        json.getJSONObject(i)
                                                .getJSONObject("end_location")
                                                .getDouble("lng"));
                                polylineOptions.add(endMarker);
                            }

                            if(polyline!=null) {
                                polyline.remove();
                                destMarker.remove();
                            }

                            polyline = googleMap.addPolyline(polylineOptions);
                            polyline.setWidth(12);
                            polyline.setColor(0xff0000ff);

                            // Put a new destination marker
                            destMarker = googleMap.addMarker(new MarkerOptions().position(endMarker)
                                    .title(from.getText().toString()));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 15));

                        } catch(Exception e) {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Map JSON Error",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), "New direction error",
                        Toast.LENGTH_SHORT).show();
                System.out.println(error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
