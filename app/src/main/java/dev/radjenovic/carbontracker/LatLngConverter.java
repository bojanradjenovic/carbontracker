package dev.radjenovic.carbontracker;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class LatLngConverter {
    @TypeConverter
    public static String fromLatLngList(List<LatLng> latLngList) {
        JSONArray jsonArray = new JSONArray();
        for (LatLng latLng : latLngList) {
            JSONArray point = new JSONArray();
            try {
                point.put(latLng.latitude);
                point.put(latLng.longitude);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            jsonArray.put(point);
        }
        return jsonArray.toString();
    }

    @TypeConverter
    public static List<LatLng> toLatLngList(String latLngString) {
        List<LatLng> latLngList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(latLngString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray point = jsonArray.getJSONArray(i);
                LatLng latLng = new LatLng(point.getDouble(0), point.getDouble(1));
                latLngList.add(latLng);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return latLngList;
    }
}
