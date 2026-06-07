package com.weatherwise.model

import com.google.gson.annotations.SerializedName

data class RainViewerResponse(
    @SerializedName("version")
    val version: String,
    @SerializedName("generated")
    val generated: Long,
    @SerializedName("host")
    val host: String,
    @SerializedName("radar")
    val radar: RadarData,
    @SerializedName("satellite")
    val satellite: SatelliteData
)

data class RadarData(
    @SerializedName("past")
    val past: List<MapFrame>,
    @SerializedName("nowcast")
    val nowcast: List<MapFrame>
)

data class SatelliteData(
    @SerializedName("infrared")
    val infrared: List<MapFrame>
)

data class MapFrame(
    @SerializedName("time")
    val time: Long,
    @SerializedName("path")
    val path: String
)