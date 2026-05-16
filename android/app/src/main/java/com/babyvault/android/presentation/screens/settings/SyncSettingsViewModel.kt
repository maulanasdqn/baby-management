package com.babyvault.android.presentation.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.model.SyncStatus
import com.babyvault.android.domain.usecase.ConfigureSyncServerUseCase
import com.babyvault.android.domain.usecase.GetSyncStatusUseCase
import com.babyvault.android.domain.usecase.SyncNowUseCase
import com.babyvault.android.worker.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncSettingsUiState(
    val status: SyncStatus? = null,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class SyncSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configureSyncServer: ConfigureSyncServerUseCase,
    private val syncNow: SyncNowUseCase,
    private val getStatus: GetSyncStatusUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SyncSettingsUiState())
    val state: StateFlow<SyncSettingsUiState> = _state.asStateFlow()

    init {
        loadStatus()
    }

    fun loadStatus() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            getStatus()
                .onSuccess { _state.value = SyncSettingsUiState(status = it, isLoading = false) }
                .onFailure { _state.value = SyncSettingsUiState(isLoading = false, error = it.message) }
        }
    }

    fun configure(serverUrl: String, apiKey: String) {
        if (serverUrl.isBlank() || apiKey.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, saved = false)
            configureSyncServer(serverUrl.trim(), apiKey.trim())
                .onSuccess {
                    SyncWorker.schedule(context)
                    loadStatus()
                    _state.value = _state.value.copy(saved = true)
                }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, error = null)
            syncNow()
                .onSuccess { _state.value = _state.value.copy(status = it, isSyncing = false) }
                .onFailure { _state.value = _state.value.copy(isSyncing = false, error = it.message) }
        }
    }
}
