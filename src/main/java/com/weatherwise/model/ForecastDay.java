package com.weatherwise.model;

public class ForecastDay {

    // ── Field lama (tidak berubah) ─────────────────────────────
    private String dayName;
    private String date;
    private String condition;
    private String description;
    private String conditionIcon;
    private String iconLiteral;
    private String iconColor;
    private double tempHigh;
    private double tempLow;
    private int    humidity;
    private double windSpeed;

    // ── Field baru dari One Call 3.0 daily[] ──────────────────
    private double pop;         // "pop" — probabilitas hujan (0.0–1.0)
    private double uvIndex;     // "uvi" — UV index puncak hari itu
    private double rain;        // "rain" — volume hujan mm
    private double snow;        // "snow" — volume salju mm
    private double windGust;    // "wind_gust" — angin maksimal m/s
    private int    windDeg;     // "wind_deg" — arah angin dalam derajat
    private double dewPoint;    // "dew_point" — titik embun °C
    private int    clouds;      // "clouds" — tutupan awan %
    private long   sunrise;     // Unix timestamp
    private long   sunset;      // Unix timestamp
    private double moonPhase;   // 0.0–1.0
    private String summary;     // deskripsi harian human-readable dari OWM

    // Suhu per waktu (dari objek "temp" One Call 3.0)
    private double tempMorn;
    private double tempDay;
    private double tempEve;
    private double tempNight;

    public ForecastDay() {}

    // ── Getters & Setters lama (tidak berubah) ─────────────────
    public String getDayName()           { return dayName; }
    public void   setDayName(String v)   { this.dayName = v; }

    public String getDate()              { return date; }
    public void   setDate(String v)      { this.date = v; }

    public String getCondition()         { return condition; }
    public void   setCondition(String v) { this.condition = v; }

    public String getDescription()       { return description; }
    public void   setDescription(String v){ this.description = v; }

    public String getConditionIcon()          { return conditionIcon; }
    public void   setConditionIcon(String v)  { this.conditionIcon = v; }

    public String getIconLiteral()       { return iconLiteral; }
    public void   setIconLiteral(String v){ this.iconLiteral = v; }

    public String getIconColor()         { return iconColor; }
    public void   setIconColor(String v) { this.iconColor = v; }

    public double getTempHigh()          { return tempHigh; }
    public void   setTempHigh(double v)  { this.tempHigh = v; }

    public double getTempLow()           { return tempLow; }
    public void   setTempLow(double v)   { this.tempLow = v; }

    public int    getHumidity()          { return humidity; }
    public void   setHumidity(int v)     { this.humidity = v; }

    public double getWindSpeed()         { return windSpeed; }
    public void   setWindSpeed(double v) { this.windSpeed = v; }

    // Alias dipakai ForecastRowCard
    public int    getHighTemp()          { return (int) tempHigh; }
    public int    getLowTemp()           { return (int) tempLow; }

    // ── Getters & Setters baru ─────────────────────────────────
    public double getPop()               { return pop; }
    public void   setPop(double v)       { this.pop = v; }

    public double getUvIndex()           { return uvIndex; }
    public void   setUvIndex(double v)   { this.uvIndex = v; }

    public double getRain()              { return rain; }
    public void   setRain(double v)      { this.rain = v; }

    public double getSnow()              { return snow; }
    public void   setSnow(double v)      { this.snow = v; }

    public double getWindGust()          { return windGust; }
    public void   setWindGust(double v)  { this.windGust = v; }

    public int    getWindDeg()           { return windDeg; }
    public void   setWindDeg(int v)      { this.windDeg = v; }

    public double getDewPoint()          { return dewPoint; }
    public void   setDewPoint(double v)  { this.dewPoint = v; }

    public int    getClouds()            { return clouds; }
    public void   setClouds(int v)       { this.clouds = v; }

    public long   getSunrise()           { return sunrise; }
    public void   setSunrise(long v)     { this.sunrise = v; }

    public long   getSunset()            { return sunset; }
    public void   setSunset(long v)      { this.sunset = v; }

    public double getMoonPhase()         { return moonPhase; }
    public void   setMoonPhase(double v) { this.moonPhase = v; }

    public String getSummary()           { return summary; }
    public void   setSummary(String v)   { this.summary = v; }

    public double getTempMorn()          { return tempMorn; }
    public void   setTempMorn(double v)  { this.tempMorn = v; }

    public double getTempDay()           { return tempDay; }
    public void   setTempDay(double v)   { this.tempDay = v; }

    public double getTempEve()           { return tempEve; }
    public void   setTempEve(double v)   { this.tempEve = v; }

    public double getTempNight()         { return tempNight; }
    public void   setTempNight(double v) { this.tempNight = v; }

    // ── Display helpers baru ───────────────────────────────────
    /** Probabilitas hujan dalam persen, mis. "60%" */
    public String getPopDisplay() {
        return (int)(pop * 100) + "%";
    }

    /** Fase bulan dalam teks */
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

    /** Arah angin dalam teks mata angin, mis. "NE" */
    public String getWindDegDisplay() {
        String[] dirs = {"N","NE","E","SE","S","SW","W","NW"};
        return dirs[(int) Math.round(windDeg / 45.0) % 8];
    }
}