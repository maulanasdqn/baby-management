package com.babyvault.android.presentation.screens.unlock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.data.local.KeystoreMasterKeyStore
import com.babyvault.android.domain.usecase.UnlockVaultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UnlockState {
    data object Idle : UnlockState
    data object Unlocking : UnlockState
    data object Unlocked : UnlockState
    data class Error(val message: String) : UnlockState
}

private val WRAPPED_KEY = byteArrayPreferencesKey("wrapped_master_key")

@HiltViewModel
class UnlockViewModel @Inject constructor(
    private val unlockVault: UnlockVaultUseCase,
    private val keystoreStore: KeystoreMasterKeyStore,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val _state = MutableStateFlow<UnlockState>(UnlockState.Idle)
    val state: StateFlow<UnlockState> = _state.asStateFlow()

    fun onBiometricSuccess() {
        viewModelScope.launch {
            _state.value = UnlockState.Unlocking
            val prefs = dataStore.data.first()
            val wrapped = prefs[WRAPPED_KEY]
            if (wrapped == null) {
                _state.value = UnlockState.Error("No vault key found. Re-initialize the app.")
                return@launch
            }
            val rawKey = runCatching { keystoreStore.unwrapKey(wrapped) }.getOrElse {
                _state.value = UnlockState.Error("Failed to unwrap key: ${it.message}")
                return@launch
            }
            unlockVault(rawKey).fold(
                onSuccess = { _state.value = UnlockState.Unlocked },
                onFailure = { _state.value = UnlockState.Error(it.message ?: "Unknown error") },
            )
        }
    }
}
