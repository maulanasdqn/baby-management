package com.babyvault.android.presentation.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.domain.usecase.CreateMilestoneUseCase
import com.babyvault.android.domain.usecase.DeleteMilestoneUseCase
import com.babyvault.android.domain.usecase.ListMilestonesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class TimelineUiState(
    val milestones: List<Milestone> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val listMilestones: ListMilestonesUseCase,
    private val createMilestone: CreateMilestoneUseCase,
    private val deleteMilestone: DeleteMilestoneUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TimelineUiState())
    val state: StateFlow<TimelineUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            listMilestones(limit = 50)
                .onSuccess { _state.value = TimelineUiState(milestones = it, isLoading = false) }
                .onFailure { _state.value = TimelineUiState(isLoading = false, error = it.message) }
        }
    }

    fun create(title: String, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            createMilestone(title.trim(), description.trim(), Instant.now())
                .onSuccess { load() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            deleteMilestone(id)
                .onSuccess { load() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }
}
