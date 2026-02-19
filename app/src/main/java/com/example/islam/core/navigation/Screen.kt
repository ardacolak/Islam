package com.example.islam.core.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PrayerTimes : Screen("prayer_times")
    object Dhikr : Screen("dhikr")
    object Settings : Screen("settings")
}
