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
        val CITY                = stringPreferencesKey("city")
        val COUNTRY             = stringPreferencesKey("country")
        val LATITUDE            = doublePreferencesKey("latitude")
        val LONGITUDE           = doublePreferencesKey("longitude")
        val CALC_METHOD         = intPreferencesKey("calculation_method")
        val NOTIFICATIONS       = booleanPreferencesKey("notifications_enabled")
        val USE_GPS             = booleanPreferencesKey("use_gps")
        val DARK_THEME          = booleanPreferencesKey("dark_theme")
        val SCHOOL              = intPreferencesKey("school")       // 0=Şafii, 1=Hanefi
        val LANGUAGE            = stringPreferencesKey("language")  // "tr" | "en" | "ar"
        val ONBOARDING_DONE     = booleanPreferencesKey("onboarding_done")
        // Namaz takibi
        val PRAYER_STREAK       = intPreferencesKey("prayer_streak")
        val STREAK_LAST_DATE    = stringPreferencesKey("streak_last_date")
        val COMPLETED_PRAYERS   = stringPreferencesKey("completed_prayers_today") // "date|prayer1,prayer2"
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            UserPreferences(
                city                 = prefs[Keys.CITY] ?: "Istanbul",
                country              = prefs[Keys.COUNTRY] ?: "Turkey",
                latitude             = prefs[Keys.LATITUDE] ?: 41.0082,
                longitude            = prefs[Keys.LONGITUDE] ?: 28.9784,
                calculationMethod    = prefs[Keys.CALC_METHOD] ?: 13,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
                useGps               = prefs[Keys.USE_GPS] ?: false,
                darkTheme            = prefs[Keys.DARK_THEME] ?: false,
                school               = prefs[Keys.SCHOOL] ?: 0,
                language             = prefs[Keys.LANGUAGE] ?: "tr"
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

    suspend fun updateSchool(school: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.SCHOOL] = school }
    }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { prefs -> prefs[Keys.LANGUAGE] = language }
    }

    // ── Onboarding ────────────────────────────────────────────────────────────

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[Keys.ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs -> prefs[Keys.ONBOARDING_DONE] = true }
    }

    // ── Namaz Takibi ─────────────────────────────────────────────────────────

    /** Bugün tamamlanan namaz id'leri (virgülle ayrılmış), format: "YYYY-MM-DD|id1,id2" */
    val completedPrayersToday: Flow<Set<String>> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val today = java.time.LocalDate.now().toString()
            val raw = prefs[Keys.COMPLETED_PRAYERS] ?: ""
            if (raw.startsWith(today)) {
                raw.substringAfter("|").split(",").filter { it.isNotBlank() }.toSet()
            } else emptySet()
        }

    val prayerStreak: Flow<Int> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[Keys.PRAYER_STREAK] ?: 0 }

    suspend fun togglePrayerCompleted(prayerId: String, allPrayerIds: List<String>) {
        val today = java.time.LocalDate.now().toString()
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.COMPLETED_PRAYERS] ?: ""
            val currentSet = if (raw.startsWith(today)) {
                raw.substringAfter("|").split(",").filter { it.isNotBlank() }.toMutableSet()
            } else mutableSetOf()

            if (prayerId in currentSet) currentSet.remove(prayerId)
            else currentSet.add(prayerId)

            prefs[Keys.COMPLETED_PRAYERS] = "$today|${currentSet.joinToString(",")}"

            // Eğer tüm namazlar tamamlandıysa streak artır
            if (currentSet.containsAll(allPrayerIds)) {
                val lastDate = prefs[Keys.STREAK_LAST_DATE] ?: ""
                val yesterday = java.time.LocalDate.now().minusDays(1).toString()
                val currentStreak = prefs[Keys.PRAYER_STREAK] ?: 0
                if (lastDate != today) {
                    prefs[Keys.PRAYER_STREAK] = if (lastDate == yesterday) currentStreak + 1 else 1
                    prefs[Keys.STREAK_LAST_DATE] = today
                }
            }
        }
    }
}
