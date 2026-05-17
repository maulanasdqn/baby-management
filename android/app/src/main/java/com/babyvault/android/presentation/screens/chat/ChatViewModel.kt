package com.babyvault.android.presentation.screens.chat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.ai.LocalAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val modelReady: Boolean = false,
)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val ai: LocalAiService,
) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state
    private var nextId = 0L
    private var genJob: Job? = null
    init {
        _state.update { it.copy(modelReady = ai.isModelReady()) }
    }
    fun send(text: String) {
        if (text.isBlank() || _state.value.isGenerating) return
        val userMsg = ChatMessage(id = nextId++, sender = Sender.User, text = text.trim())
        val placeholder = ChatMessage(id = nextId++, sender = Sender.Assistant, text = "", isStreaming = true)
        _state.update { it.copy(messages = it.messages + userMsg + placeholder, isGenerating = true) }
        val assistantId = placeholder.id
        genJob = viewModelScope.launch {
            val sb = StringBuilder()
            ai.generate(buildPrompt(userMsg.text)).collect { token ->
                sb.append(token)
                _state.update { s ->
                    s.copy(messages = s.messages.map { m ->
                        if (m.id == assistantId) m.copy(text = sb.toString()) else m
                    })
                }
            }
            _state.update { s ->
                s.copy(
                    isGenerating = false,
                    messages = s.messages.map { m ->
                        if (m.id == assistantId) m.copy(isStreaming = false) else m
                    },
                )
            }
        }
    }
    fun cancelGeneration() {
        genJob?.cancel()
        _state.update { s ->
            s.copy(
                isGenerating = false,
                messages = s.messages.map { m ->
                    if (m.isStreaming) m.copy(isStreaming = false) else m
                },
            )
        }
    }
    fun refreshModelStatus() {
        _state.update { it.copy(modelReady = ai.isModelReady()) }
    }
    private fun buildPrompt(userText: String): String {
        val history = _state.value.messages
            .filterNot { it.isStreaming }
            .takeLast(10)
            .joinToString("\n") { m ->
                if (m.sender == Sender.User) "User: ${m.text}" else "Assistant: ${m.text}"
            }
        return "$history\nUser: $userText\nAssistant:"
    }
}
