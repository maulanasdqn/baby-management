package com.babyvault.android.presentation.screens.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.model.MediaItem
import com.babyvault.android.domain.usecase.ListMediaUseCase
import com.babyvault.android.domain.usecase.StoreMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaUiState(
    val items: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val storeMedia: StoreMediaUseCase,
    private val listMedia: ListMediaUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MediaUiState())
    val state: StateFlow<MediaUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            listMedia()
                .onSuccess { _state.value = MediaUiState(items = it, isLoading = false) }
                .onFailure { _state.value = MediaUiState(isLoading = false, error = it.message) }
        }
    }

    fun store(title: String, bytes: ByteArray) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true, error = null)
            storeMedia(title, bytes)
                .onSuccess { load() }
                .onFailure { _state.value = _state.value.copy(isUploading = false, error = it.message) }
        }
    }
}
