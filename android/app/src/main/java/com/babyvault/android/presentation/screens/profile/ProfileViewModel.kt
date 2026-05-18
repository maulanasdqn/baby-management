package com.babyvault.android.presentation.screens.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.data.local.BabyProfile
import com.babyvault.android.data.local.BabyProfileStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@Immutable
data class ProfileUiState(
    val profile: BabyProfile? = null,
    val isLoading: Boolean = true,
    val saved: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileStore: BabyProfileStore,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            profileStore.profile.collect { profile ->
                _state.value = ProfileUiState(profile = profile, isLoading = false)
            }
        }
    }

    fun save(name: String, dobMillis: Long) {
        viewModelScope.launch {
            profileStore.save(name, dobMillis)
            _state.value = _state.value.copy(saved = true)
        }
    }

    fun savePhoto(uri: Uri) {
        viewModelScope.launch {
            val dest = File(context.filesDir, "profile_photo.jpg")
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { inp ->
                    dest.outputStream().use { out -> inp.copyTo(out) }
                }
            }
            profileStore.savePhoto(dest.absolutePath)
        }
    }

    fun resetSaved() {
        _state.value = _state.value.copy(saved = false)
    }
}
