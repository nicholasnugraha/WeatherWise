package com.weatherwise.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class GeocodingResponse {

    @SerializedName("name")        public String            name;
    @SerializedName("local_names") public Map<String, String> localNames; // nama lokal per bahasa
    @SerializedName("lat")         public double            lat;
    @SerializedName("lon")         public double            lon;
    @SerializedName("country")     public String            country;
    @SerializedName("state")       public String            state;   // bisa null

    // Helper — ambil nama dalam Bahasa Indonesia jika ada
    public String getDisplayName() {
        if (localNames != null && localNames.containsKey("id")) {
            return localNames.get("id");
        }
        return name;
    }

    // Helper — format tampilan lengkap: "Jakarta, DKI Jakarta, ID"
    public String getFullDisplayName() {
        StringBuilder sb = new StringBuilder(name);
        if (state != null && !state.isEmpty()) sb.append(", ").append(state);
        if (country != null && !country.isEmpty()) sb.append(", ").append(country);
        return sb.toString();
    }
}