package com.weatherwise.model;

import java.util.List;

/**
 * Model utama untuk response One Call API 3.0.
 * Menampung semua section: current, hourly, daily, dan alerts.
 * Ref: https://openweathermap.org/api/one-call-3
 */
public class OneCallResponse {

    // ── Root fields ───────────────────────────────────────────
    private double lat;
    private double lon;
    private String timezone;        // mis. "Asia/Jakarta"
    private int    timezoneOffset;  // shift dari UTC dalam detik

    // ── Sections ──────────────────────────────────────────────
    private CurrentData        current;
    private List<HourlyData>   hourly;   // 48 jam ke depan
    private List<DailyData>    daily;    // 8 hari ke depan
    private List<AlertData>    alerts;   // bisa null jika tidak ada peringatan

    // ══════════════════════════════════════════════════════════
    // Inner class: CurrentData
    // Memetakan section "current" dari One Call 3.0
    // ══════════════════════════════════════════════════════════
    public static class CurrentData {
        private long   dt;           // Unix timestamp
        private long   sunrise;      // Unix timestamp
        private long   sunset;       // Unix timestamp
        private double temp;
        private double feelsLike;
        private int    pressure;
        private int    humidity;
        private double dewPoint;
        private int    clouds;       // cloudiness %
        private double uvi;          // UV index
        private int    visibility;   // meter, max 10000
        private double windSpeed;
        private double windGust;     // bisa 0 jika tidak tersedia
        private int    windDeg;      // arah angin dalam derajat
        private double rain1h;       // mm/h, bisa 0 jika tidak hujan
        private double snow1h;       // mm/h, bisa 0 jika tidak salju
        private String conditionMain;        // mis. "Rain"
        private String conditionDescription; // mis. "light rain"
        private String conditionIcon;        // mis. "10d"

        // ── Getters & Setters ──────────────────────────────────
        public long   getDt()                    { return dt; }
        public void   setDt(long v)              { this.dt = v; }

        public long   getSunrise()               { return sunrise; }
        public void   setSunrise(long v)         { this.sunrise = v; }

        public long   getSunset()                { return sunset; }
        public void   setSunset(long v)          { this.sunset = v; }

        public double getTemp()                  { return temp; }
        public void   setTemp(double v)          { this.temp = v; }

        public double getFeelsLike()             { return feelsLike; }
        public void   setFeelsLike(double v)     { this.feelsLike = v; }

        public int    getPressure()              { return pressure; }
        public void   setPressure(int v)         { this.pressure = v; }

        public int    getHumidity()              { return humidity; }
        public void   setHumidity(int v)         { this.humidity = v; }

        public double getDewPoint()              { return dewPoint; }
        public void   setDewPoint(double v)      { this.dewPoint = v; }

        public int    getClouds()                { return clouds; }
        public void   setClouds(int v)           { this.clouds = v; }

        public double getUvi()                   { return uvi; }
        public void   setUvi(double v)           { this.uvi = v; }

        public int    getVisibility()            { return visibility; }
        public void   setVisibility(int v)       { this.visibility = v; }

        public double getWindSpeed()             { return windSpeed; }
        public void   setWindSpeed(double v)     { this.windSpeed = v; }

        public double getWindGust()              { return windGust; }
        public void   setWindGust(double v)      { this.windGust = v; }

        public int    getWindDeg()               { return windDeg; }
        public void   setWindDeg(int v)          { this.windDeg = v; }

        public double getRain1h()                { return rain1h; }
        public void   setRain1h(double v)        { this.rain1h = v; }

        public double getSnow1h()                { return snow1h; }
        public void   setSnow1h(double v)        { this.snow1h = v; }

        public String getConditionMain()         { return conditionMain; }
        public void   setConditionMain(String v) { this.conditionMain = v; }

        public String getConditionDescription()          { return conditionDescription; }
        public void   setConditionDescription(String v)  { this.conditionDescription = v; }

        public String getConditionIcon()         { return conditionIcon; }
        public void   setConditionIcon(String v) { this.conditionIcon = v; }

        // ── Helper display ─────────────────────────────────────
        public String getVisibilityDisplay() {
            if (visibility <= 0) return "N/A";
            return String.format("%.1f km", visibility / 1000.0);
        }

        public String getUviDisplay() {
            if (uvi <= 0) return "0";
            return String.format("%.1f", uvi);
        }

        public String getWindDegDisplay() {
            // Konversi derajat → arah mata angin
            String[] dirs = {"N","NE","E","SE","S","SW","W","NW"};
            return dirs[(int) Math.round(windDeg / 45.0) % 8];
        }
    }

    // ══════════════════════════════════════════════════════════
    // Inner class: HourlyData
    // Memetakan tiap entry di array "hourly" (48 jam ke depan)
    // ══════════════════════════════════════════════════════════
    public static class HourlyData {
        private long   dt;
        private double temp;
        private double feelsLike;
        private int    pressure;
        private int    humidity;
        private double dewPoint;
        private double uvi;
        private int    clouds;
        private int    visibility;
        private double windSpeed;
        private double windGust;
        private int    windDeg;
        private double pop;          // probability of precipitation (0.0–1.0)
        private double rain1h;
        private double snow1h;
        private String conditionMain;
        private String conditionDescription;
        private String conditionIcon;

        public long   getDt()                    { return dt; }
        public void   setDt(long v)              { this.dt = v; }

        public double getTemp()                  { return temp; }
        public void   setTemp(double v)          { this.temp = v; }

        public double getFeelsLike()             { return feelsLike; }
        public void   setFeelsLike(double v)     { this.feelsLike = v; }

        public int    getPressure()              { return pressure; }
        public void   setPressure(int v)         { this.pressure = v; }

        public int    getHumidity()              { return humidity; }
        public void   setHumidity(int v)         { this.humidity = v; }

        public double getDewPoint()              { return dewPoint; }
        public void   setDewPoint(double v)      { this.dewPoint = v; }

        public double getUvi()                   { return uvi; }
        public void   setUvi(double v)           { this.uvi = v; }

        public int    getClouds()                { return clouds; }
        public void   setClouds(int v)           { this.clouds = v; }

        public int    getVisibility()            { return visibility; }
        public void   setVisibility(int v)       { this.visibility = v; }

        public double getWindSpeed()             { return windSpeed; }
        public void   setWindSpeed(double v)     { this.windSpeed = v; }

        public double getWindGust()              { return windGust; }
        public void   setWindGust(double v)      { this.windGust = v; }

        public int    getWindDeg()               { return windDeg; }
        public void   setWindDeg(int v)          { this.windDeg = v; }

        public double getPop()                   { return pop; }
        public void   setPop(double v)           { this.pop = v; }

        public double getRain1h()                { return rain1h; }
        public void   setRain1h(double v)        { this.rain1h = v; }

        public double getSnow1h()                { return snow1h; }
        public void   setSnow1h(double v)        { this.snow1h = v; }

        public String getConditionMain()         { return conditionMain; }
        public void   setConditionMain(String v) { this.conditionMain = v; }

        public String getConditionDescription()         { return conditionDescription; }
        public void   setConditionDescription(String v) { this.conditionDescription = v; }

        public String getConditionIcon()         { return conditionIcon; }
        public void   setConditionIcon(String v) { this.conditionIcon = v; }

        /** Mengembalikan probabilitas hujan dalam persen, mis. "60%" */
        public String getPopDisplay() {
            return (int)(pop * 100) + "%";
        }
    }

    // ══════════════════════════════════════════════════════════
    // Inner class: DailyData
    // Memetakan tiap entry di array "daily" (8 hari ke depan)
    // ══════════════════════════════════════════════════════════
    public static class DailyData {
        private long   dt;
        private long   sunrise;
        private long   sunset;
        private long   moonrise;
        private long   moonset;
        private double moonPhase;
        private String summary;      // deskripsi cuaca harian (human-readable)

        // Suhu harian — One Call 3.0 punya objek "temp" bersarang
        private double tempMorn;
        private double tempDay;
        private double tempEve;
        private double tempNight;
        private double tempMin;
        private double tempMax;

        // Feels like per waktu
        private double feelsLikeMorn;
        private double feelsLikeDay;
        private double feelsLikeEve;
        private double feelsLikeNight;

        private int    pressure;
        private int    humidity;
        private double dewPoint;
        private double windSpeed;
        private double windGust;
        private int    windDeg;
        private int    clouds;
        private double uvi;
        private double pop;          // probability of precipitation
        private double rain;         // volume mm (tidak selalu ada)
        private double snow;         // volume mm (tidak selalu ada)
        private String conditionMain;
        private String conditionDescription;
        private String conditionIcon;

        public long   getDt()                    { return dt; }
        public void   setDt(long v)              { this.dt = v; }

        public long   getSunrise()               { return sunrise; }
        public void   setSunrise(long v)         { this.sunrise = v; }

        public long   getSunset()                { return sunset; }
        public void   setSunset(long v)          { this.sunset = v; }

        public long   getMoonrise()              { return moonrise; }
        public void   setMoonrise(long v)        { this.moonrise = v; }

        public long   getMoonset()               { return moonset; }
        public void   setMoonset(long v)         { this.moonset = v; }

        public double getMoonPhase()             { return moonPhase; }
        public void   setMoonPhase(double v)     { this.moonPhase = v; }

        public String getSummary()               { return summary; }
        public void   setSummary(String v)       { this.summary = v; }

        public double getTempMorn()              { return tempMorn; }
        public void   setTempMorn(double v)      { this.tempMorn = v; }

        public double getTempDay()               { return tempDay; }
        public void   setTempDay(double v)       { this.tempDay = v; }

        public double getTempEve()               { return tempEve; }
        public void   setTempEve(double v)       { this.tempEve = v; }

        public double getTempNight()             { return tempNight; }
        public void   setTempNight(double v)     { this.tempNight = v; }

        public double getTempMin()               { return tempMin; }
        public void   setTempMin(double v)       { this.tempMin = v; }

        public double getTempMax()               { return tempMax; }
        public void   setTempMax(double v)       { this.tempMax = v; }

        public double getFeelsLikeMorn()         { return feelsLikeMorn; }
        public void   setFeelsLikeMorn(double v) { this.feelsLikeMorn = v; }

        public double getFeelsLikeDay()          { return feelsLikeDay; }
        public void   setFeelsLikeDay(double v)  { this.feelsLikeDay = v; }

        public double getFeelsLikeEve()          { return feelsLikeEve; }
        public void   setFeelsLikeEve(double v)  { this.feelsLikeEve = v; }

        public double getFeelsLikeNight()        { return feelsLikeNight; }
        public void   setFeelsLikeNight(double v){ this.feelsLikeNight = v; }

        public int    getPressure()              { return pressure; }
        public void   setPressure(int v)         { this.pressure = v; }

        public int    getHumidity()              { return humidity; }
        public void   setHumidity(int v)         { this.humidity = v; }

        public double getDewPoint()              { return dewPoint; }
        public void   setDewPoint(double v)      { this.dewPoint = v; }

        public double getWindSpeed()             { return windSpeed; }
        public void   setWindSpeed(double v)     { this.windSpeed = v; }

        public double getWindGust()              { return windGust; }
        public void   setWindGust(double v)      { this.windGust = v; }

        public int    getWindDeg()               { return windDeg; }
        public void   setWindDeg(int v)          { this.windDeg = v; }

        public int    getClouds()                { return clouds; }
        public void   setClouds(int v)           { this.clouds = v; }

        public double getUvi()                   { return uvi; }
        public void   setUvi(double v)           { this.uvi = v; }

        public double getPop()                   { return pop; }
        public void   setPop(double v)           { this.pop = v; }

        public double getRain()                  { return rain; }
        public void   setRain(double v)          { this.rain = v; }

        public double getSnow()                  { return snow; }
        public void   setSnow(double v)          { this.snow = v; }

        public String getConditionMain()         { return conditionMain; }
        public void   setConditionMain(String v) { this.conditionMain = v; }

        public String getConditionDescription()         { return conditionDescription; }
        public void   setConditionDescription(String v) { this.conditionDescription = v; }

        public String getConditionIcon()         { return conditionIcon; }
        public void   setConditionIcon(String v) { this.conditionIcon = v; }

        /** Mengembalikan probabilitas hujan dalam persen, mis. "40%" */
        public String getPopDisplay() {
            return (int)(pop * 100) + "%";
        }

        /** Deskripsi fase bulan dari nilai numerik moon_phase */
        public String getMoonPhaseDisplay() {
            if (moonPhase == 0 || moonPhase == 1) return "New Moon";
            else if (moonPhase < 0.25)            return "Waxing Crescent";
            else if (moonPhase == 0.25)           return "First Quarter";
            else if (moonPhase < 0.5)             return "Waxing Gibbous";
            else if (moonPhase == 0.5)            return "Full Moon";
            else if (moonPhase < 0.75)            return "Waning Gibbous";
            else if (moonPhase == 0.75)           return "Last Quarter";
            else                                  return "Waning Crescent";
        }
    }

    // ══════════════════════════════════════════════════════════
    // Inner class: AlertData
    // Memetakan tiap entry di array "alerts" (jika ada)
    // ══════════════════════════════════════════════════════════
    public static class AlertData {
        private String       senderName;   // mis. "BMKG"
        private String       event;        // mis. "Heavy Rain Warning"
        private long         start;        // Unix timestamp
        private long         end;          // Unix timestamp
        private String       description;
        private List<String> tags;

        public String       getSenderName()          { return senderName; }
        public void         setSenderName(String v)  { this.senderName = v; }

        public String       getEvent()               { return event; }
        public void         setEvent(String v)       { this.event = v; }

        public long         getStart()               { return start; }
        public void         setStart(long v)         { this.start = v; }

        public long         getEnd()                 { return end; }
        public void         setEnd(long v)           { this.end = v; }

        public String       getDescription()         { return description; }
        public void         setDescription(String v) { this.description = v; }

        public List<String> getTags()                { return tags; }
        public void         setTags(List<String> v)  { this.tags = v; }
    }

    // ── Root Getters & Setters ─────────────────────────────────
    public double            getLat()              { return lat; }
    public void              setLat(double v)      { this.lat = v; }

    public double            getLon()              { return lon; }
    public void              setLon(double v)      { this.lon = v; }

    public String            getTimezone()         { return timezone; }
    public void              setTimezone(String v) { this.timezone = v; }

    public int               getTimezoneOffset()         { return timezoneOffset; }
    public void              setTimezoneOffset(int v)    { this.timezoneOffset = v; }

    public CurrentData       getCurrent()          { return current; }
    public void              setCurrent(CurrentData v)   { this.current = v; }

    public List<HourlyData>  getHourly()           { return hourly; }
    public void              setHourly(List<HourlyData> v) { this.hourly = v; }

    public List<DailyData>   getDaily()            { return daily; }
    public void              setDaily(List<DailyData> v)   { this.daily = v; }

    public List<AlertData>   getAlerts()           { return alerts; }
    public void              setAlerts(List<AlertData> v)  { this.alerts = v; }
}