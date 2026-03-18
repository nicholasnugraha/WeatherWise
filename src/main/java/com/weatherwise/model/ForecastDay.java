package com.weatherwise.model;

public class ForecastDay {

    private String dayName;
    private String date;
    private String condition;
    private String description;    // ✅ baru — deskripsi detail
    private String conditionIcon;  // kode OWM, mis "10d"
    private String iconLiteral;    // ✅ baru — Ikonli literal, mis "mdi2w-weather-rainy"
    private String iconColor;      // ✅ baru — hex color, mis "#2b8cee"
    private double tempHigh;
    private double tempLow;
    private int    humidity;
    private double windSpeed;

    // ── Constructors ───────────────────────────────────────────
    public ForecastDay() {}

    // ── Getters & Setters ──────────────────────────────────────
    public String getDayName()       { return dayName; }
    public void   setDayName(String v)        { this.dayName = v; }

    public String getDate()          { return date; }
    public void   setDate(String v)           { this.date = v; }

    public String getCondition()     { return condition; }
    public void   setCondition(String v)      { this.condition = v; }

    public String getDescription()   { return description; }
    public void   setDescription(String v)    { this.description = v; }

    public String getConditionIcon() { return conditionIcon; }
    public void   setConditionIcon(String v)  { this.conditionIcon = v; }

    public String getIconLiteral()   { return iconLiteral; }
    public void   setIconLiteral(String v)    { this.iconLiteral = v; }

    public String getIconColor()     { return iconColor; }
    public void   setIconColor(String v)      { this.iconColor = v; }

    public double getTempHigh()      { return tempHigh; }
    public void   setTempHigh(double v)       { this.tempHigh = v; }

    public double getTempLow()       { return tempLow; }
    public void   setTempLow(double v)        { this.tempLow = v; }

    public int    getHumidity()      { return humidity; }
    public void   setHumidity(int v)          { this.humidity = v; }

    public double getWindSpeed()     { return windSpeed; }
    public void   setWindSpeed(double v)      { this.windSpeed = v; }

    // ── Alias — dipakai ForecastRowCard ───────────────────────
    // Mengembalikan suhu dalam format int
    public int getHighTemp() { return (int) tempHigh; }
    public int getLowTemp()  { return (int) tempLow; }
}
