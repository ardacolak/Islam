package com.example.islam.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.islam.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val CITY = stringPreferencesKey("city")
        val COUNTRY = stringPreferencesKey("country")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CALC_METHOD = intPreferencesKey("calculation_method")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val USE_GPS = booleanPreferencesKey("use_gps")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            UserPreferences(
                city = prefs[Keys.CITY] ?: "Istanbul",
                country = prefs[Keys.COUNTRY] ?: "Turkey",
                latitude = prefs[Keys.LATITUDE] ?: 41.0082,
                longitude = prefs[Keys.LONGITUDE] ?: 28.9784,
                calculationMethod = prefs[Keys.CALC_METHOD] ?: 13,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
                useGps = prefs[Keys.USE_GPS] ?: false,
                darkTheme = prefs[Keys.DARK_THEME] ?: false
            )
        }

    suspend fun updateCity(city: String, country: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CITY] = city
            prefs[Keys.COUNTRY] = country
        }
    }

    suspend fun updateCoordinates(lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LATITUDE] = lat
            prefs[Keys.LONGITUDE] = lon
        }
    }

    suspend fun updateNotifications(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun updateUseGps(useGps: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.USE_GPS] = useGps }
    }

    suspend fun updateDarkTheme(dark: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.DARK_THEME] = dark }
    }

    suspend fun updateCalculationMethod(method: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.CALC_METHOD] = method }
    }
}
