package com.weatherwise.model;

public class CurrentWeather {
    private String cityName;
    private String country;
    private double temperature;
    private double feelsLike;
    private double tempMin;
    private double tempMax;
    private int    humidity;
    private double windSpeed;
    private int    pressure;
    private String condition;      // "Clear", "Rain", dll
    private String conditionIcon;  // icon code dari OWM, mis "01d"
    private double latitude;
    private double longitude;

    // ── Constructors ──────────────────────────────────────────
    public CurrentWeather() {}

    // ── Getters & Setters ─────────────────────────────────────
    public String getCityName()      { return cityName; }
    public void   setCityName(String v)  { this.cityName = v; }

    public String getCountry()       { return country; }
    public void   setCountry(String v)   { this.country = v; }

    public double getTemperature()   { return temperature; }
    public void   setTemperature(double v) { this.temperature = v; }

    public double getFeelsLike()     { return feelsLike; }
    public void   setFeelsLike(double v)  { this.feelsLike = v; }

    public double getTempMin()       { return tempMin; }
    public void   setTempMin(double v)   { this.tempMin = v; }

    public double getTempMax()       { return tempMax; }
    public void   setTempMax(double v)   { this.tempMax = v; }

    public int    getHumidity()      { return humidity; }
    public void   setHumidity(int v)     { this.humidity = v; }

    public double getWindSpeed()     { return windSpeed; }
    public void   setWindSpeed(double v) { this.windSpeed = v; }

    public int    getPressure()      { return pressure; }
    public void   setPressure(int v)     { this.pressure = v; }

    public String getCondition()     { return condition; }
    public void   setCondition(String v) { this.condition = v; }

    public String getConditionIcon() { return conditionIcon; }
    public void   setConditionIcon(String v) { this.conditionIcon = v; }

    public double getLatitude()      { return latitude; }
    public void   setLatitude(double v)  { this.latitude = v; }

    public double getLongitude()     { return longitude; }
    public void   setLongitude(double v) { this.longitude = v; }

    // Helper: suhu dalam format tampilan
    public String getTempDisplay()   { return (int) temperature + "°C"; }
    public String getFeelsLikeDisplay() { return "Feels like " + (int) feelsLike + "°C"; }
}
