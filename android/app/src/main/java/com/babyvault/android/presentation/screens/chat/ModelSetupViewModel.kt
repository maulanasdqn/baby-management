package com.babyvault.android.presentation.screens.chat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.ai.LocalAiService
import com.babyvault.android.ai.ModelDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
data class ModelSetupState(
    val hfToken: String = com.babyvault.android.BuildConfig.HF_TOKEN,
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val statusText: String = "",
    val error: String? = null,
    val isReady: Boolean = false,
)
@HiltViewModel
class ModelSetupViewModel @Inject constructor(
    private val downloader: ModelDownloadManager,
    private val ai: LocalAiService,
) : ViewModel() {
    private val _state = MutableStateFlow(ModelSetupState())
    val state: StateFlow<ModelSetupState> = _state
    init {
        if (downloader.isDownloaded()) loadModel()
    }
    fun setToken(token: String) {
        _state.update { it.copy(hfToken = token, error = null) }
    }
    fun startDownload() {
        if (_state.value.isDownloading) return
        _state.update { it.copy(isDownloading = true, error = null, progress = 0f) }
        viewModelScope.launch {
            runCatching {
                downloader.download(_state.value.hfToken).collect { progress ->
                    val mb = progress.bytesDownloaded / 1_000_000
                    val totalMb = progress.totalBytes / 1_000_000
                    _state.update { it.copy(progress = progress.fraction, statusText = "${mb} MB / ${totalMb} MB") }
                }
            }.onFailure { e ->
                _state.update { it.copy(isDownloading = false, error = e.message ?: "Download failed") }
                return@launch
            }
            loadModel()
        }
    }
    private fun loadModel() {
        ai.loadModel(downloader.modelDir)
            .onSuccess { _state.update { it.copy(isDownloading = false, isReady = true) } }
            .onFailure { e -> _state.update { it.copy(isDownloading = false, error = e.message) } }
    }
}
