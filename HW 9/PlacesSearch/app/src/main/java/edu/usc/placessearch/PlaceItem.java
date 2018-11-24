package edu.usc.placessearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utkar on 4/24/2018.
 */

public class PlaceItem {
    private String iconURI;
    private String placeName;
    private float lat;
    private float lng;
    private String placeId;
    private int utility;
    private String address;

    public String getIconURI() {
        return iconURI;
    }

    public String getPlaceName() {
        return placeName;
    }

    public int getUtility() {
        return utility;
    }

    public void setIconURI(String iconURI) {
        this.iconURI = iconURI;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setUtility(int utility) {
        this.utility = utility;
    }

    public PlaceItem(String placeName, String id, String iconURI, float lat, float lng,
                     int utility, String address) {
        this.placeId = id;
        this.placeName = placeName;
        this.iconURI = iconURI;
        this.lat = lat;
        this.lng = lng;
        this.utility = utility;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    static List<PlaceItem> convertObjectList(JSONArray results) throws JSONException{
        List<PlaceItem> result = new ArrayList<>();

        for(int i=0; i<results.length(); i++) {
            JSONObject t = results.getJSONObject(i);
            JSONObject location = t.getJSONObject("geometry").getJSONObject("location");
            result.add(new PlaceItem(t.getString("name"), t.getString("place_id"),
                    t.getString("icon"), (float)location.getDouble("lat"),
                    (float) location.getDouble("lng"), 1, t.getString("vicinity")));
        }

        return result;
    }
}
