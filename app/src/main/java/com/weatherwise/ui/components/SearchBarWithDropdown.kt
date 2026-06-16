package com.weatherwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.weatherwise.model.GeocodingResponse

@Composable
fun SearchBarWithDropdown(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    suggestions: List<GeocodingResponse>,
    onSuggestionClick: (GeocodingResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { 
                onQueryChange(it)
                isDropdownExpanded = it.isNotBlank()
            },
            placeholder = { Text("Cari kota...", color = Color.White.copy(alpha = 0.6f)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.6f))
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { 
                        onQueryChange("")
                        isDropdownExpanded = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White.copy(alpha = 0.8f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (isDropdownExpanded && suggestions.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopStart,
                properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .background(Color(0xFF1E1E2E), RoundedCornerShape(12.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                    ) {
                        items(suggestions) { suggestion ->
                            SuggestionItem(
                                suggestion = suggestion,
                                onClick = {
                                    onSuggestionClick(suggestion)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: GeocodingResponse,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color(0xFF90CAF9),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = suggestion.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            val region = listOfNotNull(suggestion.state, suggestion.country).joinToString(", ")
            Text(
                text = region,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}