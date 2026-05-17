package com.babyvault.android.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.ai.InferenceStatus
import com.babyvault.android.ai.LocalAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject

data class ModelSetupState(
    val isModelPresent: Boolean = false,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadFile: String = "",
    val isReady: Boolean = false,
    val error: String? = null,
)

private val MODEL_FILES = listOf(
    "vocab.json" to "https://huggingface.co/openai-community/gpt2/resolve/main/vocab.json",
    "merges.txt" to "https://huggingface.co/openai-community/gpt2/resolve/main/merges.txt",
    "model.safetensors" to "https://huggingface.co/openai-community/gpt2/resolve/main/model.safetensors",
)

@HiltViewModel
class ModelSetupViewModel @Inject constructor(
    private val ai: LocalAiService,
) : ViewModel() {

    private val _state = MutableStateFlow(ModelSetupState())
    val state: StateFlow<ModelSetupState> = _state

    fun check() {
        val present = ai.isModelDownloaded()
        _state.update { it.copy(isModelPresent = present) }
        if (present && !ai.isReady()) loadModel()
    }

    fun deleteAndRedownload() {
        viewModelScope.launch(Dispatchers.IO) {
            ai.modelDir().listFiles()?.forEach { it.delete() }
            _state.update { ModelSetupState() }
            download()
        }
    }

    fun download() {
        if (_state.value.isDownloading) return
        _state.update { it.copy(isDownloading = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val dir = ai.modelDir().also { it.mkdirs() }
            for ((name, url) in MODEL_FILES) {
                _state.update { it.copy(downloadFile = name, downloadProgress = 0f) }
                val dest = File(dir, name)
                try {
                    downloadFile(url, dest) { progress ->
                        _state.update { it.copy(downloadProgress = progress) }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(isDownloading = false, error = "Download failed ($name): ${e.message}") }
                    return@launch
                }
            }
            _state.update { it.copy(isDownloading = false, isModelPresent = true) }
            loadModel()
        }
    }

    private fun loadModel() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            when (val status = ai.initialize()) {
                is InferenceStatus.Ready ->
                    _state.update { it.copy(isLoading = false, isReady = true) }
                is InferenceStatus.ModelMissing ->
                    _state.update { it.copy(isLoading = false, isModelPresent = false) }
                is InferenceStatus.Error ->
                    _state.update { it.copy(isLoading = false, error = status.reason) }
            }
        }
    }

    private fun downloadFile(url: String, dest: File, onProgress: (Float) -> Unit) {
        val conn = URL(url).openConnection().apply { connect() }
        val total = conn.contentLengthLong
        conn.getInputStream().use { input ->
            dest.outputStream().use { output ->
                val buf = ByteArray(8 * 1024)
                var downloaded = 0L
                var n: Int
                while (input.read(buf).also { n = it } != -1) {
                    output.write(buf, 0, n)
                    downloaded += n
                    if (total > 0) onProgress(downloaded.toFloat() / total)
                }
            }
        }
    }
}
