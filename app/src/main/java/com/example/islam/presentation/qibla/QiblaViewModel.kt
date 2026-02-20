package com.example.islam.presentation.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.services.CompassData
import com.example.islam.services.CompassTracker
import com.example.islam.utils.QiblaCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QiblaUiState(
    val compass: CompassData? = null,
    val hasSensor: Boolean = true,
    val isLoading: Boolean = true,
    val locationName: String = "",
    val distanceKm: Int = 0
)

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val compassTracker: CompassTracker,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    init {
        observeAndTrack()
    }

    /**
     * Kullanıcı konumu değiştiğinde yeni koordinatlarla sensör akışını yeniden başlatır.
     * collectLatest, önceki koordinatlarla çalışan Flow'u otomatik iptal eder.
     */
    private fun observeAndTrack() {
        viewModelScope.launch {
            prefsDataStore.userPreferences.collectLatest { prefs ->
                _uiState.update { state ->
                    state.copy(
                        locationName = "${prefs.city}, ${prefs.country}",
                        distanceKm = QiblaCalculator.distanceToKaabaKm(prefs.latitude, prefs.longitude)
                    )
                }
                compassTracker
                    .track(prefs.latitude, prefs.longitude)
                    .catch { _uiState.update { it.copy(hasSensor = false, isLoading = false) } }
                    .collect { data ->
                        _uiState.update { it.copy(compass = data, isLoading = false) }
                    }
            }
        }
    }
}
