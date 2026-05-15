package com.weatherwise.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CurrentWeatherResponse {

    @SerializedName("name")
    public String cityName;

    @SerializedName("dt")
    public long timestamp;

    @SerializedName("timezone")
    public int timezoneOffset;       // detik dari UTC

    @SerializedName("coord")
    public Coord coord;

    @SerializedName("main")
    public Main main;

    @SerializedName("weather")
    public List<Weather> weather;

    @SerializedName("wind")
    public Wind wind;

    @SerializedName("visibility")
    public int visibility;           // meter

    @SerializedName("sys")
    public Sys sys;

    @SerializedName("clouds")
    public Clouds clouds;

    // ── Nested classes ─────────────────────────────────────────
    public static class Coord {
        @SerializedName("lat") public double lat;
        @SerializedName("lon") public double lon;
    }

    public static class Main {
        @SerializedName("temp")       public double temp;
        @SerializedName("feels_like") public double feelsLike;
        @SerializedName("temp_min")   public double tempMin;
        @SerializedName("temp_max")   public double tempMax;
        @SerializedName("pressure")   public int    pressure;
        @SerializedName("humidity")   public int    humidity;
    }

    public static class Weather {
        @SerializedName("id")          public int    id;
        @SerializedName("main")        public String main;
        @SerializedName("description") public String description;
        @SerializedName("icon")        public String icon;
    }

    public static class Wind {
        @SerializedName("speed") public double speed;
        @SerializedName("deg")   public int    deg;
        @SerializedName("gust")  public double gust;
    }

    public static class Sys {
        @SerializedName("country") public String country;
        @SerializedName("sunrise") public long   sunrise;
        @SerializedName("sunset")  public long   sunset;
    }

    public static class Clouds {
        @SerializedName("all") public int all;    // persentase tutupan awan
    }
}