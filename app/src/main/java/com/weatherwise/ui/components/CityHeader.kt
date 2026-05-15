package com.weatherwise.ui.components
@Composable
fun CityHeader(cityName: String, country: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = cityName,
            fontSize   = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color.White
        )
        if (country.isNotEmpty()) {
            Text(country, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}