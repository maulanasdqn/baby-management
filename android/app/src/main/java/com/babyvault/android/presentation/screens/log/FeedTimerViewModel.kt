package com.babyvault.android.presentation.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.model.FeedType
import com.babyvault.android.domain.usecase.LogFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedTimerState(
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val side: String = "Left",
) {
    val elapsedLabel: String get() {
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }
}

@HiltViewModel
class FeedTimerViewModel @Inject constructor(private val logFeed: LogFeedUseCase) : ViewModel() {
    private val _state = MutableStateFlow(FeedTimerState())
    val state: StateFlow<FeedTimerState> = _state

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

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
            _saved.value = true
        }
    }

    fun setSide(side: String) { _state.update { it.copy(side = side) } }

    override fun onCleared() { timerJob?.cancel() }
}
