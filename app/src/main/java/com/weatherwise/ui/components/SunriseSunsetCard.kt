package com.weatherwise.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Brightness3
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherwise.model.CurrentWeather
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SunriseSunsetCard(
    weather  : CurrentWeather,
    modifier : Modifier = Modifier
) {
    // Format timestamp Unix → "HH:mm" sesuai timezone device
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    val zone = ZoneId.systemDefault()

    val sunriseText = remember(weather.sunrise) {
        if (weather.sunrise > 0)
            Instant.ofEpochSecond(weather.sunrise).atZone(zone).format(fmt)
        else "--:--"
    }
    val sunsetText = remember(weather.sunset) {
        if (weather.sunset > 0)
            Instant.ofEpochSecond(weather.sunset).atZone(zone).format(fmt)
        else "--:--"
    }

    // Hitung posisi matahari saat ini (0.0 = sunrise, 1.0 = sunset)
    val sunProgress = remember(weather.sunrise, weather.sunset) {
        val now = System.currentTimeMillis() / 1000L
        when {
            weather.sunrise <= 0 || weather.sunset <= 0 -> 0.5f
            now <= weather.sunrise -> 0f
            now >= weather.sunset  -> 1f
            else -> (now - weather.sunrise).toFloat() /
                    (weather.sunset - weather.sunrise).toFloat()
        }
    }

    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Label ──────────────────────────────────────────
            Text(
                text     = "Matahari",
                fontSize = 12.sp,
                color    = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // ── Arc Matahari ───────────────────────────────────
            SunArcCanvas(
                progress = sunProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Sunrise & Sunset Info ──────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Sunrise (kiri)
                SunTimeItem(
                    icon    = Icons.Outlined.WbSunny,
                    label   = "Terbit",
                    time    = sunriseText,
                    color   = Color(0xFFFFA726),
                    isLeft  = true
                )

                // Durasi siang
                val daylightMinutes = remember(weather.sunrise, weather.sunset) {
                    if (weather.sunrise > 0 && weather.sunset > 0)
                        ((weather.sunset - weather.sunrise) / 60).toInt()
                    else 0
                }
                if (daylightMinutes > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = "${daylightMinutes / 60}j ${daylightMinutes % 60}m",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color      = Color.White
                        )
                        Text(
                            text     = "Durasi siang",
                            fontSize = 10.sp,
                            color    = Color.White.copy(alpha = 0.55f)
                        )
                    }
                }

                // Sunset (kanan)
                SunTimeItem(
                    icon    = Icons.Outlined.Brightness3,
                    label   = "Terbenam",
                    time    = sunsetText,
                    color   = Color(0xFFFF7043),
                    isLeft  = false
                )
            }
        }
    }
}

// ── Canvas: Busur lengkung + titik matahari bergerak ──────────
@Composable
private fun SunArcCanvas(
    progress : Float,
    modifier : Modifier = Modifier
) {
    // Animasi halus saat progress berubah
    val animatedProgress by animateFloatAsState(
        targetValue  = progress,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label        = "sun_progress"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val startX = 0f
        val endX   = w
        val peakY  = 0f          // puncak busur di atas
        val baseY  = h           // dasar busur di bawah

        // ── Busur background (abu-abu tipis) ──────────────────
        val arcPath = Path().apply {
            moveTo(startX, baseY)
            cubicTo(
                w * 0.25f, peakY,
                w * 0.75f, peakY,
                endX,      baseY
            )
        }
        drawPath(
            path  = arcPath,
            color = Color.White.copy(alpha = 0.2f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // ── Busur progress (terang, sesuai posisi matahari) ───
        // Gunakan segmen path dari 0 sampai progress
        val progressPath = Path().apply {
            moveTo(startX, baseY)
            // Bezier cubic — ambil titik pada t = animatedProgress
            val t  = animatedProgress
            val cp1x = w * 0.25f; val cp1y = peakY
            val cp2x = w * 0.75f; val cp2y = peakY

            // De Casteljau untuk titik akhir segmen pada t
            val p1x = lerp(startX, cp1x, t)
            val p1y = lerp(baseY,  cp1y, t)
            val p2x = lerp(cp1x,   cp2x, t)
            val p2y = lerp(cp1y,   cp2y, t)
            val p3x = lerp(cp2x,   endX, t)
            val p3y = lerp(cp2y,   baseY, t)

            val q1x = lerp(p1x, p2x, t)
            val q1y = lerp(p1y, p2y, t)
            val q2x = lerp(p2x, p3x, t)
            val q2y = lerp(p2y, p3y, t)

            val sunX = lerp(q1x, q2x, t)
            val sunY = lerp(q1y, q2y, t)

            cubicTo(p1x, p1y, q1x, q1y, sunX, sunY)

            // Simpan posisi matahari untuk lingkaran di bawah
            this.close()

            // Gambar ulang hanya segmen (bukan path tertutup)
            rewind()
            moveTo(startX, baseY)
            cubicTo(p1x, p1y, q1x, q1y, sunX, sunY)
        }
        drawPath(
            path  = progressPath,
            color = Color(0xFFFFA726).copy(alpha = 0.85f),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // ── Titik matahari bergerak ────────────────────────────
        val t  = animatedProgress
        val cp1x = w * 0.25f; val cp1y = peakY
        val cp2x = w * 0.75f; val cp2y = peakY

        val p1x = lerp(startX, cp1x, t); val p1y = lerp(baseY, cp1y, t)
        val p2x = lerp(cp1x,   cp2x, t); val p2y = lerp(cp1y, cp2y, t)
        val p3x = lerp(cp2x,   endX, t); val p3y = lerp(cp2y, baseY, t)
        val q1x = lerp(p1x, p2x, t);     val q1y = lerp(p1y, p2y, t)
        val q2x = lerp(p2x, p3x, t);     val q2y = lerp(p2y, p3y, t)
        val sunX = lerp(q1x, q2x, t)
        val sunY = lerp(q1y, q2y, t)

        // Lingkaran luar (glow)
        drawCircle(
            color  = Color(0xFFFFA726).copy(alpha = 0.3f),
            radius = 10.dp.toPx(),
            center = Offset(sunX, sunY)
        )
        // Lingkaran dalam (inti matahari)
        drawCircle(
            color  = Color(0xFFFFC107),
            radius = 6.dp.toPx(),
            center = Offset(sunX, sunY)
        )
    }
}

// ── Item waktu sunrise / sunset ───────────────────────────────
@Composable
private fun SunTimeItem(
    icon   : androidx.compose.ui.graphics.vector.ImageVector,
    label  : String,
    time   : String,
    color  : Color,
    isLeft : Boolean
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = if (isLeft) Arrangement.Start else Arrangement.End
    ) {
        if (isLeft) {
            Icon(icon, contentDescription = label,
                tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Column(
            horizontalAlignment = if (isLeft) Alignment.Start else Alignment.End
        ) {
            Text(
                text       = time,
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White
            )
            Text(
                text     = label,
                fontSize = 11.sp,
                color    = Color.White.copy(alpha = 0.6f)
            )
        }
        if (!isLeft) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(icon, contentDescription = label,
                tint = color, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Lerp helper untuk kalkulasi posisi Bezier ─────────────────
private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t