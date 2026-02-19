package com.example.islam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.islam.core.navigation.NavGraph
import com.example.islam.core.navigation.Screen
import com.example.islam.notification.NotificationHelper
import com.example.islam.presentation.settings.SettingsViewModel
import com.example.islam.ui.theme.IslamTheme
import com.example.islam.worker.PrayerTimeUpdateWorker
import dagger.hilt.android.AndroidEntryPoint

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Ana Sayfa", Icons.Default.Home),
    BottomNavItem(Screen.PrayerTimes, "Vakitler", Icons.Outlined.AccessTime),
    BottomNavItem(Screen.Dhikr, "Zikir", Icons.Outlined.Favorite),
    BottomNavItem(Screen.Settings, "Ayarlar", Icons.Default.Settings)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // Schedule daily WorkManager task
        PrayerTimeUpdateWorker.enqueueDailyWork(this)

        setContent {
            IslamApp()
        }
    }
}

@Composable
fun IslamApp() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val darkTheme = settingsState.preferences.darkTheme

    IslamTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = { IslamBottomNavBar(navController) }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavGraph(navController = navController)
            }
        }
    }
}

@Composable
private fun IslamBottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
