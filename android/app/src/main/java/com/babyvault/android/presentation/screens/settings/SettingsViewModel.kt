package com.babyvault.android.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.data.local.BabyProfile
import com.babyvault.android.data.local.BabyProfileStore
import com.babyvault.android.domain.model.SyncStatus
import com.babyvault.android.domain.usecase.GetSyncStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    profileStore: BabyProfileStore,
    private val getSyncStatus: GetSyncStatusUseCase,
) : ViewModel() {

    val profile: StateFlow<BabyProfile?> = profileStore.profile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    private val _syncStatus = MutableStateFlow<SyncStatus?>(null)
    val syncStatus: StateFlow<SyncStatus?> = _syncStatus

    init {
        viewModelScope.launch {
            getSyncStatus().onSuccess { _syncStatus.value = it }
        }
    }
}
