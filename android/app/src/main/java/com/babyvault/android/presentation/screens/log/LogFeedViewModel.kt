package com.babyvault.android.presentation.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.model.FeedType
import com.babyvault.android.domain.usecase.LogFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogFeedViewModel @Inject constructor(
    private val logFeed: LogFeedUseCase,
) : ViewModel() {
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun save(feedType: FeedType, amountMl: Int?, durationMinutes: Int?, side: String?, notes: String) {
        viewModelScope.launch {
            logFeed(feedType, amountMl, durationMinutes, side, notes)
            _saved.value = true
        }
    }
}
