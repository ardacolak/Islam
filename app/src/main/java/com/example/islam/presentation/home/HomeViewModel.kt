package com.example.islam.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.core.util.DateUtil
import com.example.islam.core.util.Resource
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.UserPreferences
import com.example.islam.domain.usecase.prayer.GetNextPrayerUseCase
import com.example.islam.domain.usecase.prayer.GetPrayerTimesUseCase
import com.example.islam.domain.usecase.prayer.NextPrayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val prayerTime: PrayerTime? = null,
    val nextPrayer: NextPrayer? = null,
    val countdownText: String = "00:00:00",
    val todayDateText: String = "",
    val error: String? = null,
    val userPreferences: UserPreferences = UserPreferences()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
    private val getNextPrayerUseCase: GetNextPrayerUseCase,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(todayDateText = DateUtil.formatDateLong()) }
        observePreferences()
        startCountdownTicker()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            prefsDataStore.userPreferences.collect { prefs ->
                _uiState.update { it.copy(userPreferences = prefs) }
                loadPrayerTimes(prefs)
            }
        }
    }

    private suspend fun loadPrayerTimes(prefs: UserPreferences) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        when (val result = getPrayerTimesUseCase(prefs.city, prefs.country, prefs.calculationMethod)) {
            is Resource.Success -> {
                val pt = result.data
                val next = getNextPrayerUseCase(pt)
                _uiState.update {
                    it.copy(isLoading = false, prayerTime = pt, nextPrayer = next)
                }
            }
            is Resource.Error -> {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
            is Resource.Loading -> Unit
        }
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val next = _uiState.value.nextPrayer
                if (next != null) {
                    val remaining = next.millisUntil - 1_000L
                    if (remaining > 0) {
                        val updated = next.copy(millisUntil = remaining)
                        _uiState.update {
                            it.copy(
                                nextPrayer = updated,
                                countdownText = DateUtil.formatCountdown(remaining)
                            )
                        }
                    } else {
                        // Prayer time arrived — reload to get new next prayer
                        _uiState.value.prayerTime?.let { pt ->
                            val newNext = getNextPrayerUseCase(pt)
                            _uiState.update {
                                it.copy(
                                    nextPrayer = newNext,
                                    countdownText = DateUtil.formatCountdown(newNext.millisUntil)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadPrayerTimes(_uiState.value.userPreferences)
        }
    }
}
