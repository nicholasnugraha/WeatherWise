package com.weatherwise.model

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("list") val list: List<ForecastItem>,
    @SerializedName("city") val city: CityInfo
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("dt_txt") val dt_txt: String,
    @SerializedName("main") val main: MainInfo,
    @SerializedName("weather") val weather: List<WeatherInfo>,
    @SerializedName("wind") val wind: WindInfo
)

data class CityInfo(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String
)

data class MainInfo(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feels_like: Double,
    @SerializedName("temp_min") val temp_min: Double,
    @SerializedName("temp_max") val temp_max: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("pressure") val pressure: Int
)

data class WeatherInfo(
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WindInfo(
    @SerializedName("speed") val speed: Double
)

// UI Models for Forecast
data class ForecastHourly(
    val dt: Long,
    val temp: Double,
    val condition: String,
    val conditionIcon: String,
    val humidity: Int,
    val windSpeed: Double
)

data class ForecastDaily(
    val dt: Long,
    val dateStr: String,
    val tempMax: Double,
    val tempMin: Double,
    val condition: String,
    val conditionIcon: String,
    val humidity: Int
)
