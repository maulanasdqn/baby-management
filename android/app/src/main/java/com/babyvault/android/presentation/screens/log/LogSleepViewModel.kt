package com.babyvault.android.presentation.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.usecase.LogSleepUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogSleepViewModel @Inject constructor(private val logSleep: LogSleepUseCase) : ViewModel() {
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun save(startMillis: Long, endMillis: Long, notes: String) {
        viewModelScope.launch {
            logSleep(startMillis, endMillis, notes)
            _saved.value = true
        }
    }
}
