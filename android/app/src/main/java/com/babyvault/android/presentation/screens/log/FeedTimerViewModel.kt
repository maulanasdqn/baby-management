package com.babyvault.android.presentation.screens.log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.model.FeedType
import com.babyvault.android.domain.usecase.LogFeedUseCase
import com.babyvault.android.presentation.utils.formatElapsedSeconds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
data class FeedTimerState(
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val side: String = "Left",
) {
    val elapsedLabel: String get() = formatElapsedSeconds(elapsedSeconds)
}
@HiltViewModel
class FeedTimerViewModel @Inject constructor(private val logFeed: LogFeedUseCase) : ViewModel() {
    private val _state = MutableStateFlow(FeedTimerState())
    val state: StateFlow<FeedTimerState> = _state
    private val _saved = Channel<Unit>(Channel.BUFFERED)
    val saved = _saved.receiveAsFlow()
    private var timerJob: Job? = null
    fun start() {
        if (_state.value.isRunning) return
        _state.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_state.value.isRunning) {
                delay(1000)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }
    fun pause() {
        _state.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }
    fun stop() {
        pause()
        val elapsed = _state.value.elapsedSeconds
        val side = _state.value.side
        viewModelScope.launch {
            logFeed(
                FeedType.BREAST,
                amountMl = null,
                durationMinutes = (elapsed / 60).toInt().coerceAtLeast(1),
                side = side,
                notes = "",
            )
            _saved.send(Unit)
        }
    }
    fun setSide(side: String) { _state.update { it.copy(side = side) } }
    override fun onCleared() { timerJob?.cancel() }
}
