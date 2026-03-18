package com.weatherwise.model;

public class ForecastDay {
    private final String dayName;
    private final String date;
    private final String iconLiteral;
    private final String iconColor;
    private final String condition;
    private final String description;
    private final int    highTemp;
    private final int    lowTemp;

    public ForecastDay(String dayName, String date, String iconLiteral,
                       String iconColor, String condition,
                       String description, int highTemp, int lowTemp) {
        this.dayName     = dayName;
        this.date        = date;
        this.iconLiteral = iconLiteral;
        this.iconColor   = iconColor;
        this.condition   = condition;
        this.description = description;
        this.highTemp    = highTemp;
        this.lowTemp     = lowTemp;
    }

    public String getDayName()     { return dayName;     }
    public String getDate()        { return date;        }
    public String getIconLiteral() { return iconLiteral; }
    public String getIconColor()   { return iconColor;   }
    public String getCondition()   { return condition;   }
    public String getDescription() { return description; }
    public int    getHighTemp()    { return highTemp;    }
    public int    getLowTemp()     { return lowTemp;     }
}