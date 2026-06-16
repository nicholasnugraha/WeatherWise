package com.weatherwise.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val cityName: String,
    val lat: Double,
    val lon: Double,
    val temperature: Double,
    val condition: String,
    val conditionIcon: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long
)
