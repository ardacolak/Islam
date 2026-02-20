package com.example.islam.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.presentation.home.HomeScreen
import com.example.islam.presentation.onboarding.OnboardingScreen
import com.example.islam.presentation.prayer.PrayerScreen
import com.example.islam.presentation.qibla.QiblaScreen
import com.example.islam.presentation.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    prefsDataStore: UserPreferencesDataStore
) {
    // Onboarding tamamlandı mı? null = henüz DataStore'dan okumadık (splash bekleme durumu)
    val onboardingDone by prefsDataStore.onboardingCompleted.collectAsState(initial = null)

    // DataStore henüz yanıt vermediyse boş kutu göster (genellikle <1 frame)
    if (onboardingDone == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val startDestination = if (onboardingDone == true) Screen.Home.route
                           else Screen.Onboarding.route

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.PrayerTimes.route) {
            PrayerScreen()
        }
        composable(Screen.Dhikr.route) {
        }
        composable(Screen.Qibla.route) {
            QiblaScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
