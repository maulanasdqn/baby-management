package com.babyvault.android.presentation.screens.splash

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.data.local.KeystoreMasterKeyStore
import com.babyvault.android.domain.usecase.GenerateMasterKeyUseCase
import com.babyvault.android.domain.usecase.UnlockVaultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashState {
    data object Loading : SplashState
    data object Ready : SplashState
    data object NeedUnlock : SplashState
}

private val WRAPPED_KEY = byteArrayPreferencesKey("wrapped_master_key")

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val generateMasterKey: GenerateMasterKeyUseCase,
    private val unlockVault: UnlockVaultUseCase,
    private val keystoreStore: KeystoreMasterKeyStore,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        viewModelScope.launch { initialize() }
    }

    private suspend fun initialize() {
        val prefs = dataStore.data.first()
        val wrapped = prefs[WRAPPED_KEY]

        if (wrapped == null) {
            // First launch — generate and persist key, then unlock immediately.
            val rawKey = generateMasterKey().getOrElse {
                _state.value = SplashState.NeedUnlock
                return
            }
            val wrappedKey = keystoreStore.wrapKey(rawKey)
            dataStore.updateData { it.toMutablePreferences().also { p -> p[WRAPPED_KEY] = wrappedKey } }
            unlockVault(rawKey)
            _state.value = SplashState.Ready
        } else {
            // Subsequent launch — biometric prompt needed; route to UnlockScreen.
            _state.value = SplashState.NeedUnlock
        }
    }
}
