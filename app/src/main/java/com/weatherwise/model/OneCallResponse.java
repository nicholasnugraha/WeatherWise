package com.weatherwise.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OneCallResponse {

    @SerializedName("lat")             public double lat;
    @SerializedName("lon")             public double lon;
    @SerializedName("timezone")        public String timezone;
    @SerializedName("timezone_offset") public int    timezoneOffset;

    @SerializedName("current")  public CurrentData        current;
    @SerializedName("hourly")   public List<HourlyData>   hourly;
    @SerializedName("daily")    public List<DailyData>    daily;
    @SerializedName("alerts")   public List<AlertData>    alerts;   // bisa null

    // ── Current ────────────────────────────────────────────────
    public static class CurrentData {
        @SerializedName("dt")         public long          dt;
        @SerializedName("sunrise")    public long          sunrise;
        @SerializedName("sunset")     public long          sunset;
        @SerializedName("temp")       public double        temp;
        @SerializedName("feels_like") public double        feelsLike;
        @SerializedName("pressure")   public int           pressure;
        @SerializedName("humidity")   public int           humidity;
        @SerializedName("uvi")        public double        uvi;
        @SerializedName("visibility") public int           visibility;
        @SerializedName("wind_speed") public double        windSpeed;
        @SerializedName("wind_deg")   public int           windDeg;
        @SerializedName("weather")    public List<Weather> weather;
    }

    // ── Hourly ─────────────────────────────────────────────────
    public static class HourlyData {
        @SerializedName("dt")         public long          dt;
        @SerializedName("temp")       public double        temp;
        @SerializedName("feels_like") public double        feelsLike;
        @SerializedName("humidity")   public int           humidity;
        @SerializedName("wind_speed") public double        windSpeed;
        @SerializedName("pop")        public double        pop;     // probabilitas hujan 0-1
        @SerializedName("weather")    public List<Weather> weather;

        // Helper — ambil icon langsung tanpa null check berulang
        public String getConditionIcon() {
            return (weather != null && !weather.isEmpty())
                    ? weather.get(0).icon : "01d";
        }
    }

    // ── Daily ──────────────────────────────────────────────────
    public static class DailyData {
        @SerializedName("dt")      public long          dt;
        @SerializedName("sunrise") public long          sunrise;
        @SerializedName("sunset")  public long          sunset;
        @SerializedName("temp")    public Temp          temp;
        @SerializedName("humidity")public int           humidity;
        @SerializedName("wind_speed") public double     windSpeed;
        @SerializedName("uvi")     public double        uvi;
        @SerializedName("pop")     public double        pop;
        @SerializedName("weather") public List<Weather> weather;
        @SerializedName("summary") public String        summary; // OWM 3.0

        public String getConditionIcon() {
            return (weather != null && !weather.isEmpty())
                    ? weather.get(0).icon : "01d";
        }

        public String getDescription() {
            return (weather != null && !weather.isEmpty())
                    ? weather.get(0).description : "";
        }
    }

    // ── Alert ──────────────────────────────────────────────────
    public static class AlertData {
        @SerializedName("sender_name") public String senderName;
        @SerializedName("event")       public String event;
        @SerializedName("start")       public long   start;
        @SerializedName("end")         public long   end;
        @SerializedName("description") public String description;
    }

    // ── Shared ─────────────────────────────────────────────────
    public static class Temp {
        @SerializedName("day")   public double day;
        @SerializedName("min")   public double min;
        @SerializedName("max")   public double max;
        @SerializedName("night") public double night;
        @SerializedName("eve")   public double eve;
        @SerializedName("morn")  public double morn;
    }

    public static class Weather {
        @SerializedName("id")          public int    id;
        @SerializedName("main")        public String main;
        @SerializedName("description") public String description;
        @SerializedName("icon")        public String icon;
    }
}