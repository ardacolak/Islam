package com.example.islam.presentation.home

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.core.util.DateUtil
import com.example.islam.core.util.Resource
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.DailyQuote
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.UserPreferences
import com.example.islam.domain.utils.LocationTracker
import com.example.islam.domain.usecase.prayer.GetNextPrayerUseCase
import com.example.islam.domain.usecase.prayer.GetPrayerTimesUseCase
import com.example.islam.domain.usecase.prayer.NextPrayer
import com.example.islam.domain.usecase.quote.GetDailyQuoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val userPreferences: UserPreferences = UserPreferences(),
    val dailyQuote: DailyQuote? = null,
    /** true olduğunda namaz vakitleri yüklenir; false iken izin ekranı gösterilir */
    val permissionsGranted: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
    private val getNextPrayerUseCase: GetNextPrayerUseCase,
    private val getDailyQuoteUseCase: GetDailyQuoteUseCase,
    private val prefsDataStore: UserPreferencesDataStore,
    private val locationTracker: LocationTracker,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            todayDateText = DateUtil.formatDateLong(),
            // İzinler zaten verilmişse doğrudan başlat — ekranda yanıp sönme olmaz
            permissionsGranted = appContext.areAllPermissionsGranted(),
            // Günlük ayet/hadis init'te hemen yüklenir (senkron, IO yok)
            dailyQuote = getDailyQuoteUseCase()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value.permissionsGranted) observePreferences()
        startCountdownTicker()
    }

    /**
     * Presentation katmanı tüm izinler alındıktan sonra bu metodu çağırır.
     * İdempotent: zaten granted ise ikinci çağrı işlemsiz döner.
     */
    fun onPermissionsGranted() {
        if (_uiState.value.permissionsGranted) return
        _uiState.update { it.copy(permissionsGranted = true) }
        observePreferences()
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

        // GPS modu aktifse konumu al ve DataStore'a yaz (Kıble hesabı için)
        if (prefs.useGps) {
            locationTracker.getCurrentLocation()?.let { loc ->
                prefsDataStore.updateCoordinates(loc.latitude, loc.longitude)
            }
        }

        when (val result = getPrayerTimesUseCase(
            city   = prefs.city,
            country = prefs.country,
            method = prefs.calculationMethod,
            school = prefs.school
        )) {
            is Resource.Success -> {
                val pt   = result.data
                val next = getNextPrayerUseCase(pt)
                _uiState.update {
                    it.copy(
                        isLoading     = false,
                        prayerTime    = pt,
                        nextPrayer    = next,
                        countdownText = DateUtil.formatCountdown(next.millisUntil)
                    )
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
                val next = _uiState.value.nextPrayer ?: continue
                val remaining = next.millisUntil - 1_000L
                if (remaining > 0) {
                    _uiState.update {
                        it.copy(
                            nextPrayer    = next.copy(millisUntil = remaining),
                            countdownText = DateUtil.formatCountdown(remaining)
                        )
                    }
                } else {
                    // Namaz vakti geldi → bir sonrakini hesapla
                    _uiState.value.prayerTime?.let { pt ->
                        val newNext = getNextPrayerUseCase(pt)
                        _uiState.update {
                            it.copy(
                                nextPrayer    = newNext,
                                countdownText = DateUtil.formatCountdown(newNext.millisUntil)
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { loadPrayerTimes(_uiState.value.userPreferences) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // İzin kontrolü — init'te senkron çalışır, UI flash'ını önler
    // ─────────────────────────────────────────────────────────────────────────

    private fun Context.areAllPermissionsGranted(): Boolean {
        val locationOk =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        val notificationOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else true

        val alarmOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(AlarmManager::class.java)).canScheduleExactAlarms()
        } else true

        return locationOk && notificationOk && alarmOk
    }
}
