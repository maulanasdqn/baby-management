package com.babyvault.android.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.core.vault.VaultEngineProvider
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.domain.usecase.ListMilestonesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val engineVersion: String = "",
    val milestones: List<Milestone> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val listMilestones: ListMilestonesUseCase,
    private val engineProvider: VaultEngineProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val version = engineProvider.engine.engineVersion()
        val milestones = listMilestones(limit = 5).getOrDefault(emptyList())
        _state.value = HomeUiState(engineVersion = version, milestones = milestones, isLoading = false)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            load()
        }
    }
}
