package com.babyvault.android.presentation.screens.growth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.model.GrowthLog
import com.babyvault.android.domain.usecase.DeleteGrowthUseCase
import com.babyvault.android.domain.usecase.ListGrowthByRangeUseCase
import com.babyvault.android.domain.usecase.LogGrowthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
data class GrowthUiState(
    val logs: List<GrowthLog> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)
@HiltViewModel
class GrowthViewModel @Inject constructor(
    private val logGrowth: LogGrowthUseCase,
    private val listByRange: ListGrowthByRangeUseCase,
    private val deleteGrowth: DeleteGrowthUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(GrowthUiState())
    val state: StateFlow<GrowthUiState> = _state.asStateFlow()
    init { load() }
    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            listByRange(from = Instant.EPOCH, to = Instant.now().plusSeconds(86_400))
                .onSuccess { logs ->
                    _state.value = GrowthUiState(logs = logs.sortedByDescending { it.loggedAt }, isLoading = false)
                }
                .onFailure { _state.value = GrowthUiState(isLoading = false, error = it.message) }
        }
    }
    fun log(weightGramsStr: String, heightMmStr: String, notes: String) {
        val weightGrams = weightGramsStr.trimEnd().toIntOrNull()
        val heightMm = heightMmStr.trimEnd().toIntOrNull()
        if (weightGrams == null && heightMm == null && notes.isBlank()) return
        viewModelScope.launch {
            logGrowth(weightGrams, heightMm, notes)
                .onSuccess { load() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }
    fun delete(id: String) {
        viewModelScope.launch {
            deleteGrowth(id)
                .onSuccess { load() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }
}
