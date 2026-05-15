package com.weatherwise.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    query         : String,
    onQueryChange : (String) -> Unit,
    onSearch      : () -> Unit,
    modifier      : Modifier = Modifier,
    placeholder   : String   = "Cari kota...",
) {
    val focusManager   = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier          = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Input Field ────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    // Lebih terang saat fokus, semi-transparan saat tidak fokus
                    if (isFocused) Color.White.copy(alpha = 0.95f)
                    else           Color.White.copy(alpha = 0.25f)
                )
        ) {
            TextField(
                value         = query,
                onValueChange = onQueryChange,
                placeholder   = {
                    Text(
                        text     = placeholder,
                        fontSize = 15.sp,
                        color    = if (isFocused) Color.Gray
                        else           Color.White.copy(alpha = 0.75f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint               = if (isFocused) Color(0xFF1565C0) else Color.White.copy(alpha = 0.8f),
                        modifier           = Modifier.size(20.dp)
                    )
                },
                // Tombol clear — hanya tampil saat ada teks
                trailingIcon = {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter   = fadeIn() + scaleIn(),
                        exit    = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector        = Icons.Default.Clear,
                                contentDescription = "Hapus",
                                tint               = if (isFocused) Color.Gray else Color.White.copy(alpha = 0.7f),
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words, // auto-capitalize nama kota
                    imeAction      = ImeAction.Search              // tombol keyboard → Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (query.isNotBlank()) {
                            onSearch()
                            focusManager.clearFocus() // tutup keyboard setelah search
                        }
                    }
                ),
                singleLine = true,
                colors     = TextFieldDefaults.colors(
                    // Warna teks berubah sesuai fokus
                    focusedTextColor        = Color(0xFF1A1C1E),
                    unfocusedTextColor      = Color.White,
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,   // hilangkan garis bawah
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = Color(0xFF1565C0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused }
            )
        }

        // ── Tombol Search (muncul saat fokus dan ada teks) ─────
        AnimatedVisibility(
            visible = isFocused && query.isNotBlank(),
            enter   = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit    = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            Button(
                onClick = {
                    onSearch()
                    focusManager.clearFocus()
                },
                shape  = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor   = Color(0xFF1565C0)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("Cari", fontSize = 14.sp)
            }
        }
    }
}