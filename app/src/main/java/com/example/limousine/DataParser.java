package com.example.limousine;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    public List<List<HashMap<String, String>>> parse(JSONObject jObject){
         List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try{
            jRoutes = jObject.getJSONArray("routes");

            for(int i = 0; i < jRoutes.length(); i++){
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                for (int j=0; j<jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List list = decodePoly(polyline);

                        for (int l = 0; l < list.size(); l++) { // Use 'l' instead of 'i'
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude)); // Use 'l' instead of '1'
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude)); // Use 'l' instead of '1'
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        } catch (Exception e){
        }
        return routes;
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63; // Convert ASCII to decimal value
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(lat / 1E5, lng / 1E5); // Convert to LatLng format
            poly.add(p);
        }

        return poly;
    }
}
