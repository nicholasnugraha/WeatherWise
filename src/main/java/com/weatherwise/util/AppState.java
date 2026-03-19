package com.weatherwise.util;

public class AppState {

    private static volatile AppState instance;

    private double  lat      = -6.2088;
    private double  lon      = 106.8456;
    private String  cityName = "Jakarta, ID";
    private boolean changed  = false;

    private AppState() {}

    public static AppState getInstance() {
        if (instance == null) {
            synchronized (AppState.class) {
                if (instance == null) instance = new AppState();
            }
        }
        return instance;
    }

    public synchronized void setLocation(double lat, double lon, String cityName) {
        this.lat      = lat;
        this.lon      = lon;
        this.cityName = cityName;
        this.changed  = true;
    }

    public synchronized double  getLat()      { return lat; }
    public synchronized double  getLon()      { return lon; }
    public synchronized String  getCityName() { return cityName; }
    public synchronized boolean isChanged()   { return changed; }
    public synchronized void    clearChanged(){ this.changed = false; }
}