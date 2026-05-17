package com.babyvault.android.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.ai.AiCoreStatus
import com.babyvault.android.ai.LocalAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelSetupState(
    val isChecking: Boolean = false,
    val isReady: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ModelSetupViewModel @Inject constructor(
    private val ai: LocalAiService,
) : ViewModel() {

    private val _state = MutableStateFlow(ModelSetupState())
    val state: StateFlow<ModelSetupState> = _state

    fun initialize() {
        if (_state.value.isChecking || _state.value.isReady) return
        _state.update { it.copy(isChecking = true, error = null) }
        viewModelScope.launch {
            when (val status = ai.initialize()) {
                is AiCoreStatus.Available ->
                    _state.update { it.copy(isChecking = false, isReady = true) }
                is AiCoreStatus.Unavailable ->
                    _state.update { it.copy(isChecking = false, error = status.reason) }
            }
        }
    }
}
