package com.example.islam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.core.i18n.AppStrings
import com.example.islam.core.i18n.stringsFor
import com.example.islam.core.navigation.NavGraph
import com.example.islam.core.navigation.Screen
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.notification.NotificationHelper
import com.example.islam.presentation.settings.SettingsViewModel
import com.example.islam.ui.theme.IslamTheme
import com.example.islam.worker.PrayerTimeUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// ─── Bottom nav veri modeli ────────────────────────────────────────────────────

data class BottomNavItem(
    val screen: Screen,
    val labelFn: (AppStrings) -> String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home,        { it.navHome },        Icons.Default.Home),
    BottomNavItem(Screen.PrayerTimes, { it.navPrayerTimes }, Icons.Outlined.AccessTime),
    BottomNavItem(Screen.Dhikr,       { it.navDhikr },       Icons.Outlined.Favorite),
    BottomNavItem(Screen.Qibla,       { it.navQibla },       Icons.Outlined.Explore),
    BottomNavItem(Screen.Settings,    { it.navSettings },    Icons.Default.Settings)
)

// ─── Activity ─────────────────────────────────────────────────────────────────

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefsDataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannels(this)
        PrayerTimeUpdateWorker.enqueueDailyWork(this)

        setContent {
            IslamApp(prefsDataStore = prefsDataStore)
        }
    }
}

// ─── Uygulama kökü ────────────────────────────────────────────────────────────

@Composable
fun IslamApp(prefsDataStore: UserPreferencesDataStore) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val prefs = settingsState.preferences
    val strings = stringsFor(prefs.language)
    val layoutDirection = if (prefs.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLayoutDirection provides layoutDirection
    ) {
        IslamTheme(darkTheme = prefs.darkTheme) {
            val navController  = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute   = backStackEntry?.destination?.route

            val showBottomBar = currentRoute != Screen.Onboarding.route

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        IslamBottomNavBar(navController, strings)
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavGraph(
                        navController  = navController,
                        prefsDataStore = prefsDataStore
                    )
                }
            }
        }
    }
}

// ─── Alt navigasyon çubuğu ────────────────────────────────────────────────────

@Composable
private fun IslamBottomNavBar(navController: NavController, strings: AppStrings) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            val label = item.labelFn(strings)

            val indicatorWidth by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 0.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "nav_indicator_${item.screen.route}"
            )

            NavigationBarItem(
                icon = {
                    Box {
                        Icon(item.icon, contentDescription = label)
                    }
                },
                label = { Text(label) },
                selected = isSelected,
                onClick  = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            )
        }
    }
}
