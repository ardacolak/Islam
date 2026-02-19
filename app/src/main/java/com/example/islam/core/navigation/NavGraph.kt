package com.example.islam.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.islam.presentation.dhikr.DhikrScreen
import com.example.islam.presentation.home.HomeScreen
import com.example.islam.presentation.prayer.PrayerScreen
import com.example.islam.presentation.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.PrayerTimes.route) {
            PrayerScreen()
        }
        composable(Screen.Dhikr.route) {
            DhikrScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
