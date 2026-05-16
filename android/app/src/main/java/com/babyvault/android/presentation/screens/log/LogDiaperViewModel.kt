package com.babyvault.android.presentation.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.model.DiaperType
import com.babyvault.android.domain.usecase.LogDiaperUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogDiaperViewModel @Inject constructor(private val logDiaper: LogDiaperUseCase) : ViewModel() {
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun save(diaperType: DiaperType, notes: String) {
        viewModelScope.launch {
            logDiaper(diaperType, notes)
            _saved.value = true
        }
    }
}
