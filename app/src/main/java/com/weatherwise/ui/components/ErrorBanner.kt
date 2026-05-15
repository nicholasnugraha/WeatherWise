package com.weatherwise.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ErrorBanner(
    message   : String,
    onDismiss : () -> Unit,
    modifier  : Modifier = Modifier
) {
    // AnimatedVisibility agar banner muncul/hilang dengan animasi slide
    AnimatedVisibility(
        visible = message.isNotEmpty(),
        enter   = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit    = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            shape    = RoundedCornerShape(14.dp),
            colors   = CardDefaults.cardColors(
                containerColor = Color(0xFFC62828).copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Icon error
                Icon(
                    imageVector        = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(22.dp)
                )

                // Teks pesan error — weight(1f) agar tombol X tidak terdorong keluar
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "Terjadi Kesalahan",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                    Text(
                        text     = message,
                        fontSize = 12.sp,
                        color    = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Tombol tutup
                IconButton(
                    onClick  = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Tutup pesan error",
                        tint               = Color.White.copy(alpha = 0.8f),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}