package com.weatherwise.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Komponen animasi cuaca dinamis menggunakan Lottie.
 * 
 * CATATAN PENTING UNTUK PRODUKSI:
 * URL di bawah ini adalah contoh publik. Untuk aplikasi produksi yang andal,
 * sangat disarankan untuk mengunduh file .json Lottie dan menyimpannya di 
 * folder `app/src/main/res/raw/`, lalu gunakan `LottieCompositionSpec.RawRes(R.raw.nama_file)`.
 */
@Composable
fun WeatherLottieAnimation(
    condition: String,
    modifier: Modifier = Modifier,
    speed: Float = 1.0f
) {
    // Mapping kondisi cuaca ke URL Lottie (Ganti dengan asset lokal R.raw.xxx untuk produksi)
    val lottieUrl = when {
        condition.contains("Clear", ignoreCase = true) || condition.contains("Sunny", ignoreCase = true) -> 
            "https://assets10.lottiefiles.com/packages/lf20_ya4ayb.json" // Contoh: Matahari
        condition.contains("Cloud", ignoreCase = true) -> 
            "https://assets10.lottiefiles.com/packages/lf20_3j7q5a.json" // Contoh: Awan
        condition.contains("Rain", ignoreCase = true) || condition.contains("Drizzle", ignoreCase = true) -> 
            "https://assets10.lottiefiles.com/packages/lf20_5k8w9c.json" // Contoh: Hujan
        condition.contains("Snow", ignoreCase = true) -> 
            "https://assets10.lottiefiles.com/packages/lf20_6l9x0d.json" // Contoh: Salju
        condition.contains("Thunder", ignoreCase = true) || condition.contains("Storm", ignoreCase = true) -> 
            "https://assets10.lottiefiles.com/packages/lf20_8m9y1e.json" // Contoh: Petir
        else -> 
            "https://assets10.lottiefiles.com/packages/lf20_3j7q5a.json" // Fallback: Awan
    }

    var composition by remember { mutableStateOf<com.airbnb.lottie.LottieComposition?>(null) }

    LaunchedEffect(lottieUrl) {
        // Load komposisi dari URL (untuk produksi, gunakan LottieCompositionSpec.RawRes)
        val result = com.airbnb.lottie.compose.LottieCompositionFactory.fromUrl(
            androidx.compose.ui.platform.LocalContext.current,
            lottieUrl
        )
        composition = result.value
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = speed
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}
