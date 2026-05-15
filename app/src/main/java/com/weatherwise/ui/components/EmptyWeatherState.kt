package com.weatherwise.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyWeatherState(modifier: Modifier = Modifier) {

    // Animasi fade-in + float naik turun pada icon awan
    val infiniteTransition = rememberInfiniteTransition(label = "empty_anim")
    val offsetY by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = -10f,
        animationSpec  = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.65f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_alpha"
    )

    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(top = 80.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon awan animasi float
        Icon(
            imageVector        = Icons.Outlined.CloudQueue,
            contentDescription = null,
            tint               = Color.White.copy(alpha = alpha),
            modifier           = Modifier
                .size(80.dp)
                .offset(y = offsetY.dp)
        )

        // Teks utama
        Text(
            text      = "Belum ada data cuaca",
            fontSize  = 18.sp,
            color     = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )

        // Teks petunjuk
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.Search,
                contentDescription = null,
                tint               = Color.White.copy(alpha = 0.5f),
                modifier           = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text      = "Ketik nama kota di kolom pencarian",
                fontSize  = 14.sp,
                color     = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}