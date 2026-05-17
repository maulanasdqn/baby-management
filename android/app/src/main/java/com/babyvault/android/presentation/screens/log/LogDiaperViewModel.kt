package com.babyvault.android.presentation.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.model.DiaperType
import com.babyvault.android.domain.usecase.LogDiaperUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogDiaperViewModel @Inject constructor(private val logDiaper: LogDiaperUseCase) : ViewModel() {
    private val _saved = Channel<Unit>(Channel.BUFFERED)
    val saved = _saved.receiveAsFlow()

    fun save(diaperType: DiaperType, notes: String) {
        viewModelScope.launch {
            logDiaper(diaperType, notes)
            _saved.send(Unit)
        }
    }
}
