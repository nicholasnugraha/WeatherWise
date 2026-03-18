package com.weatherwise.model;

public class Location {
    private String name;
    private String country;
    private String state;
    private double lat;
    private double lon;

    public Location() {}

    public Location(String name, String country, double lat, double lon) {
        this.name    = name;
        this.country = country;
        this.lat     = lat;
        this.lon     = lon;
    }

    public String getDisplayName() {
        return state != null && !state.isEmpty()
               ? name + ", " + state + ", " + country
               : name + ", " + country;
    }

    public String getName()    { return name; }
    public void   setName(String v)    { this.name = v; }

    public String getCountry() { return country; }
    public void   setCountry(String v) { this.country = v; }

    public String getState()   { return state; }
    public void   setState(String v)   { this.state = v; }

    public double getLat()     { return lat; }
    public void   setLat(double v)     { this.lat = v; }

    public double getLon()     { return lon; }
    public void   setLon(double v)     { this.lon = v; }
}
