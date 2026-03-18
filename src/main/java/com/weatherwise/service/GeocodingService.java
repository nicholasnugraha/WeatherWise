package com.weatherwise.service;

import com.weatherwise.model.Location;
import com.weatherwise.util.AppConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GeocodingService {

    public List<Location> searchCity(String query) throws Exception {
        String encoded = java.net.URLEncoder.encode(query, "UTF-8");
        String urlStr  = AppConfig.GEO_URL + "/direct"
                + "?q="     + encoded
                + "&limit=5"
                + "&appid=" + AppConfig.OWM_API_KEY;

        String response = fetchUrl(urlStr);
        return parseLocations(new JSONArray(response));
    }

    private List<Location> parseLocations(JSONArray arr) {
        List<Location> result = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            Location   loc = new Location();
            loc.setName(obj.getString("name"));
            loc.setCountry(obj.getString("country"));
            loc.setState(obj.optString("state", ""));
            loc.setLat(obj.getDouble("lat"));
            loc.setLon(obj.getDouble("lon"));
            result.add(loc);
        }
        return result;
    }

    private String fetchUrl(String urlStr) throws Exception {
        URL               url  = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (conn.getResponseCode() != 200)
            throw new Exception("Geocoding error: " + conn.getResponseCode());

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();
        return sb.toString();
    }
}
