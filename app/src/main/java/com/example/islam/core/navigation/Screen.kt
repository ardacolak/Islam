package com.example.islam.core.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")   // ilk açılış akışı
    object Home        : Screen("home")
    object PrayerTimes : Screen("prayer_times")
    object Dhikr       : Screen("dhikr")
    object Qibla       : Screen("qibla")
    object Settings    : Screen("settings")
}
