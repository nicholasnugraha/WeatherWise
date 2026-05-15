package com.weatherwise.map

interface TileSource {
    val id: String
    val attribution: String
    val tileBaseUrl: String
    val apiKey: String?

    fun tileUrl(x: Int, y: Int, z: Int): String {
        val normalizedBase = tileBaseUrl.trimEnd('/')
        val key = apiKey?.takeIf { it.isNotBlank() }
        val query = if (key != null) "?api_key=$key" else ""
        return "$normalizedBase/$z/$x/$y.png$query"
    }
}
