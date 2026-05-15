package com.weatherwise.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weatherwise.ui.screens.ForecastScreen
import com.weatherwise.ui.screens.HomeScreen
import com.weatherwise.viewmodel.WeatherViewModel

// Konstanta route — hindari typo string
object Routes {
    const val HOME     = "home"
    const val FORECAST = "forecast"
}

@Composable
fun AppNavigation(viewModel: WeatherViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel        = viewModel,
                onSeeFullForecast = {
                    navController.navigate(Routes.FORECAST)
                }
            )
        }

        composable(Routes.FORECAST) {
            ForecastScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}