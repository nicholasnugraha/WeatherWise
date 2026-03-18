package com.weatherwise.util;

public class AppState {

    private static AppState instance;

    private double  lat      = -6.2088;
    private double  lon      = 106.8456;
    private String  cityName = "Jakarta, ID";
    private boolean changed  = false;

    private AppState() {}

    public static AppState getInstance() {
        if (instance == null) instance = new AppState();
        return instance;
    }

    public void setLocation(double lat, double lon, String cityName) {
        this.lat      = lat;
        this.lon      = lon;
        this.cityName = cityName;
        this.changed  = true;
    }

    public double  getLat()      { return lat; }
    public double  getLon()      { return lon; }
    public String  getCityName() { return cityName; }

    /** Cek apakah lokasi berubah sejak terakhir kali Dashboard dibuka */
    public boolean isChanged()   { return changed; }

    /** Dipanggil DashboardController setelah selesai reload */
    public void clearChanged()   { this.changed = false; }
}
