package com.weatherwise.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.weatherwise.model.ForecastDaily
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DailyForecastItem(
    daily   : ForecastDaily,
    isFirst : Boolean = false,
    isLast  : Boolean = false,
    modifier: Modifier = Modifier
) {
    // State expand/collapse untuk detail tambahan
    var isExpanded by remember { mutableStateOf(false) }

    // Format hari — "Senin, 19 Mei"
    val dayLabel = remember(daily.dt) {
        Instant.ofEpochSecond(daily.dt)
            .atZone(ZoneId.systemDefault())
            .format(
                DateTimeFormatter.ofPattern("EEEE, d MMM",
                    Locale("id", "ID"))
            )
    }

    val iconUrl = "https://openweathermap.org/img/wn/${daily.conditionIcon}@2x.png"

    // Radius menyesuaikan posisi di list
    val cornerTop    = if (isFirst) 16.dp else 4.dp
    val cornerBottom = if (isLast && !isExpanded) 16.dp else 4.dp

    Card(
        shape  = RoundedCornerShape(
            topStart    = cornerTop,
            topEnd      = cornerTop,
            bottomStart = cornerBottom,
            bottomEnd   = cornerBottom
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column {

            // ── Baris utama ────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Nama hari
                Text(
                    text       = dayLabel,
                    fontSize   = 14.sp,
                    color      = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.weight(1.4f)
                )

                // Icon cuaca
                AsyncImage(
                    model              = iconUrl,
                    contentDescription = daily.condition,
                    modifier           = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Probabilitas hujan (tidak ada di ForecastDaily, jadi kita skip atau tampilkan kelembaban)
                Text(
                    text     = "${daily.humidity}%",
                    fontSize = 12.sp,
                    color    = Color(0xFF90CAF9),
                    modifier = Modifier.weight(0.5f)
                )

                // Suhu min / max
                Row(
                    modifier            = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "${daily.tempMax.toInt()}°",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                    Text(
                        text     = " / ",
                        fontSize = 13.sp,
                        color    = Color.White.copy(alpha = 0.4f)
                    )
                    Text(
                        text     = "${daily.tempMin.toInt()}°",
                        fontSize = 13.sp,
                        color    = Color.White.copy(alpha = 0.65f)
                    )
                }
            }

            // ── Detail expandable ──────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start   = 16.dp,
                            end     = 16.dp,
                            bottom  = 14.dp
                        )
                ) {
                    HorizontalDivider(
                        color    = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Grid detail 2 kolom
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier            = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow("🌡️ Tertinggi", "${daily.tempMax.toInt()}°")
                            DetailRow("🌡️ Terendah", "${daily.tempMin.toInt()}°")
                            DetailRow("💧 Kelembaban", "${daily.humidity}%")
                        }
                        Column(
                            modifier            = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow("🌧️ Kondisi", daily.condition)
                        }
                    }
                }
            }
        }
    }
}

// ── Baris label + nilai di dalam detail ───────────────────────
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text     = label,
            fontSize = 12.sp,
            color    = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text       = value,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium,
            color      = Color.White
        )
    }
}
