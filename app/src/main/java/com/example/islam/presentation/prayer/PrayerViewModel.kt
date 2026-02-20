package com.example.islam.presentation.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.core.util.Resource
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.UserPreferences
import com.example.islam.domain.model.timeFor
import com.example.islam.domain.usecase.prayer.GetPrayerTimesUseCase
import com.example.islam.domain.usecase.prayer.GetNextPrayerUseCase
import com.example.islam.core.util.DateUtil.cleanTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class PrayerUiState(
    val isLoading: Boolean = false,
    val prayerTime: PrayerTime? = null,
    val currentPrayer: Prayer? = null,
    val error: String? = null,
    val userPreferences: UserPreferences = UserPreferences(),
    val completedPrayers: Set<String> = emptySet()
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
    private val getNextPrayerUseCase: GetNextPrayerUseCase,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    // Kılınabilir namazlar (sabah akşam fark etmez, Güneş hariç)
    private val trackablePrayers = listOf(
        Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA
    ).map { it.name }

    init {
        viewModelScope.launch {
            prefsDataStore.userPreferences.collect { prefs ->
                _uiState.update { it.copy(userPreferences = prefs) }
                loadTodaysPrayers(prefs)
            }
        }
        viewModelScope.launch {
            prefsDataStore.completedPrayersToday.collect { completed ->
                _uiState.update { it.copy(completedPrayers = completed) }
            }
        }
    }

    fun togglePrayerCompleted(prayer: Prayer) {
        viewModelScope.launch {
            prefsDataStore.togglePrayerCompleted(prayer.name, trackablePrayers)
        }
    }

    private suspend fun loadTodaysPrayers(prefs: UserPreferences) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        when (val result = getPrayerTimesUseCase(prefs.city, prefs.country, prefs.calculationMethod)) {
            is Resource.Success -> {
                val pt = result.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        prayerTime = pt,
                        currentPrayer = determineCurrentPrayer(pt)
                    )
                }
            }
            is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            is Resource.Loading -> Unit
        }
    }

    private fun determineCurrentPrayer(pt: PrayerTime): Prayer? {
        val now = Calendar.getInstance()
        val orderedPrayers = listOf(
            Prayer.IMSAK, Prayer.FAJR, Prayer.SUNRISE,
            Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA
        )
        var current: Prayer? = null
        for (prayer in orderedPrayers) {
            val timeStr = pt.timeFor(prayer).cleanTime()
            val parts = timeStr.split(":")
            if (parts.size < 2) continue
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts[1].toInt())
                set(Calendar.SECOND, 0)
            }
            if (cal.before(now) || cal == now) current = prayer
        }
        return current
    }

    fun refresh() {
        viewModelScope.launch { loadTodaysPrayers(_uiState.value.userPreferences) }
    }
}
