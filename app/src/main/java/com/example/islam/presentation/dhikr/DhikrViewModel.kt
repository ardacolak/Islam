package com.example.islam.presentation.dhikr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.data.repository.DhikrRepositoryImpl
import com.example.islam.domain.model.Dhikr
import com.example.islam.domain.repository.DhikrRepository
import com.example.islam.domain.usecase.dhikr.GetDhikrListUseCase
import com.example.islam.domain.usecase.dhikr.IncrementDhikrUseCase
import com.example.islam.domain.usecase.dhikr.ResetDhikrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DhikrUiState(
    val dhikrList: List<Dhikr> = emptyList(),
    val selectedDhikr: Dhikr? = null
)

@HiltViewModel
class DhikrViewModel @Inject constructor(
    private val getDhikrListUseCase: GetDhikrListUseCase,
    private val incrementDhikrUseCase: IncrementDhikrUseCase,
    private val resetDhikrUseCase: ResetDhikrUseCase,
    private val repository: DhikrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DhikrUiState())
    val uiState: StateFlow<DhikrUiState> = _uiState.asStateFlow()

    init {
        seedAndLoad()
    }

    private fun seedAndLoad() {
        viewModelScope.launch {
            (repository as? DhikrRepositoryImpl)?.seedIfEmpty()
            getDhikrListUseCase().collect { list ->
                val selected = _uiState.value.selectedDhikr
                _uiState.update {
                    it.copy(
                        dhikrList = list,
                        selectedDhikr = if (selected != null)
                            list.find { d -> d.id == selected.id } ?: list.firstOrNull()
                        else
                            list.firstOrNull()
                    )
                }
            }
        }
    }

    fun selectDhikr(dhikr: Dhikr) {
        _uiState.update { it.copy(selectedDhikr = dhikr) }
    }

    fun increment() {
        val dhikr = _uiState.value.selectedDhikr ?: return
        viewModelScope.launch { incrementDhikrUseCase(dhikr.id) }
    }

    fun reset() {
        val dhikr = _uiState.value.selectedDhikr ?: return
        viewModelScope.launch { resetDhikrUseCase(dhikr.id) }
    }

    fun resetAll() {
        viewModelScope.launch { resetDhikrUseCase.resetAll() }
    }
}
