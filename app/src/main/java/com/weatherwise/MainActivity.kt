// MainActivity.kt — lebih clean untuk Compose entry point
package com.weatherwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.weatherwise.ui.navigation.AppNavigation
import com.weatherwise.ui.theme.WeatherWiseTheme
import com.weatherwise.viewmodel.WeatherViewModel
import com.weatherwise.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory   = WeatherViewModelFactory(application)
        val viewModel = ViewModelProvider(this, factory)[WeatherViewModel::class.java]

        setContent {
            WeatherWiseTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}