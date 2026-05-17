package com.babyvault.android.presentation.screens.log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.usecase.LogSleepUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class LogSleepViewModel @Inject constructor(private val logSleep: LogSleepUseCase) : ViewModel() {
    private val _saved = Channel<Unit>(Channel.BUFFERED)
    val saved = _saved.receiveAsFlow()
    fun save(startMillis: Long, endMillis: Long, notes: String) {
        viewModelScope.launch {
            logSleep(startMillis, endMillis, notes)
            _saved.send(Unit)
        }
    }
}
