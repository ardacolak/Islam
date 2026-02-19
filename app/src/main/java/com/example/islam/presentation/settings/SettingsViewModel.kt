package com.example.islam.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val cityInput: String = "Istanbul",
    val countryInput: String = "Turkey"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsDataStore.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        preferences = prefs,
                        cityInput = prefs.city,
                        countryInput = prefs.country
                    )
                }
            }
        }
    }

    fun onCityInputChange(city: String) = _uiState.update { it.copy(cityInput = city) }
    fun onCountryInputChange(country: String) = _uiState.update { it.copy(countryInput = country) }

    fun saveLocation() {
        viewModelScope.launch {
            prefsDataStore.updateCity(
                _uiState.value.cityInput.trim(),
                _uiState.value.countryInput.trim()
            )
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { prefsDataStore.updateNotifications(enabled) }
    }

    fun setDarkTheme(dark: Boolean) {
        viewModelScope.launch { prefsDataStore.updateDarkTheme(dark) }
    }

    fun setCalculationMethod(method: Int) {
        viewModelScope.launch { prefsDataStore.updateCalculationMethod(method) }
    }
}
