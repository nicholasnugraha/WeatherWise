package com.weatherwise.api

import com.weatherwise.model.RainViewerResponse
import retrofit2.Call
import retrofit2.http.GET

interface RainViewerApiService {
    @GET("public/weather-maps.json")
    fun getWeatherMaps(): Call<RainViewerResponse>
}