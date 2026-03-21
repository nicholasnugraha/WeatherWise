package com.weatherwise.model;

public class CurrentWeather {

    // ── Field lama (tidak berubah) ─────────────────────────────
    private String cityName;
    private String country;
    private double temperature;
    private double feelsLike;
    private double tempMin;
    private double tempMax;
    private int    humidity;
    private double windSpeed;
    private int    pressure;
    private int    visibility;
    private String condition;
    private String conditionIcon;
    private double latitude;
    private double longitude;

    // ── Field baru dari One Call 3.0 ──────────────────────────
    private double uvIndex;     // "uvi" — indeks UV saat ini
    private double dewPoint;    // "dew_point" — titik embun °C
    private double windGust;    // "wind_gust" — kecepatan angin maksimal m/s
    private int    windDeg;     // "wind_deg" — arah angin dalam derajat
    private double rain1h;      // "rain.1h" — curah hujan 1 jam mm/h
    private double snow1h;      // "snow.1h" — curah salju 1 jam mm/h
    private int    clouds;      // "clouds" — tutupan awan %
    private long   sunrise;     // Unix timestamp matahari terbit
    private long   sunset;      // Unix timestamp matahari terbenam

    public CurrentWeather() {}

    // ── Getters & Setters lama (tidak berubah) ─────────────────
    public String getCityName()           { return cityName; }
    public void   setCityName(String v)   { this.cityName = v; }

    public String getCountry()            { return country; }
    public void   setCountry(String v)    { this.country = v; }

    public double getTemperature()        { return temperature; }
    public void   setTemperature(double v){ this.temperature = v; }

    public double getFeelsLike()          { return feelsLike; }
    public void   setFeelsLike(double v)  { this.feelsLike = v; }

    public double getTempMin()            { return tempMin; }
    public void   setTempMin(double v)    { this.tempMin = v; }

    public double getTempMax()            { return tempMax; }
    public void   setTempMax(double v)    { this.tempMax = v; }

    public int    getHumidity()           { return humidity; }
    public void   setHumidity(int v)      { this.humidity = v; }

    public double getWindSpeed()          { return windSpeed; }
    public void   setWindSpeed(double v)  { this.windSpeed = v; }

    public int    getPressure()           { return pressure; }
    public void   setPressure(int v)      { this.pressure = v; }

    public int    getVisibility()         { return visibility; }
    public void   setVisibility(int v)    { this.visibility = v; }

    public String getCondition()          { return condition; }
    public void   setCondition(String v)  { this.condition = v; }

    public String getConditionIcon()      { return conditionIcon; }
    public void   setConditionIcon(String v){ this.conditionIcon = v; }

    public double getLatitude()           { return latitude; }
    public void   setLatitude(double v)   { this.latitude = v; }

    public double getLongitude()          { return longitude; }
    public void   setLongitude(double v)  { this.longitude = v; }

    // ── Getters & Setters baru ─────────────────────────────────
    public double getUvIndex()            { return uvIndex; }
    public void   setUvIndex(double v)    { this.uvIndex = v; }

    public double getDewPoint()           { return dewPoint; }
    public void   setDewPoint(double v)   { this.dewPoint = v; }

    public double getWindGust()           { return windGust; }
    public void   setWindGust(double v)   { this.windGust = v; }

    public int    getWindDeg()            { return windDeg; }
    public void   setWindDeg(int v)       { this.windDeg = v; }

    public double getRain1h()             { return rain1h; }
    public void   setRain1h(double v)     { this.rain1h = v; }

    public double getSnow1h()             { return snow1h; }
    public void   setSnow1h(double v)     { this.snow1h = v; }

    public int    getClouds()             { return clouds; }
    public void   setClouds(int v)        { this.clouds = v; }

    public long   getSunrise()            { return sunrise; }
    public void   setSunrise(long v)      { this.sunrise = v; }

    public long   getSunset()             { return sunset; }
    public void   setSunset(long v)       { this.sunset = v; }

    // ── Display helpers lama (tidak berubah) ───────────────────
    public String getVisibilityDisplay() {
        if (visibility <= 0) return "N/A";
        return String.format("%.1f km", visibility / 1000.0);
    }

    public String getTempDisplay()        { return (int) temperature + "\u00b0C"; }
    public String getFeelsLikeDisplay()   { return "Feels like " + (int) feelsLike + "\u00b0C"; }

    // ── Display helpers baru ───────────────────────────────────
    /** Mengembalikan kategori UV index, mis. "Moderate (4.2)" */
    public String getUvIndexDisplay() {
        if (uvIndex <= 0) return "0";
        String category;
        if      (uvIndex <= 2)  category = "Low";
        else if (uvIndex <= 5)  category = "Moderate";
        else if (uvIndex <= 7)  category = "High";
        else if (uvIndex <= 10) category = "Very High";
        else                    category = "Extreme";
        return category + " (" + String.format("%.1f", uvIndex) + ")";
    }

    /** Arah angin dalam teks mata angin, mis. "SW" */
    public String getWindDegDisplay() {
        String[] dirs = {"N","NE","E","SE","S","SW","W","NW"};
        return dirs[(int) Math.round(windDeg / 45.0) % 8];
    }

    /** Waktu matahari terbit dalam format HH:mm, mis. "05:48" */
    public String getSunriseDisplay(String timezone) {
        if (sunrise <= 0) return "N/A";
        return java.time.Instant.ofEpochSecond(sunrise)
            .atZone(java.time.ZoneId.of(timezone))
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }

    /** Waktu matahari terbenam dalam format HH:mm, mis. "17:55" */
    public String getSunsetDisplay(String timezone) {
        if (sunset <= 0) return "N/A";
        return java.time.Instant.ofEpochSecond(sunset)
            .atZone(java.time.ZoneId.of(timezone))
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }
}