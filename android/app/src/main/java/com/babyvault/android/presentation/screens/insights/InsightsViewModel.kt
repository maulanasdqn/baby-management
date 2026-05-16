package com.babyvault.android.presentation.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.usecase.ListDiaperByRangeUseCase
import com.babyvault.android.domain.usecase.ListFeedByRangeUseCase
import com.babyvault.android.domain.usecase.ListSleepByRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

data class InsightsState(
    val feedCount: Int = 0,
    val sleepCount: Int = 0,
    val diaperCount: Int = 0,
    val sleepMinutesByDay: Map<String, Long> = emptyMap(),
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val listFeed: ListFeedByRangeUseCase,
    private val listSleep: ListSleepByRangeUseCase,
    private val listDiaper: ListDiaperByRangeUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    private val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val to = System.currentTimeMillis()
            val from = to - (7L * 24 * 60 * 60 * 1000)

            val feeds = listFeed(from, to).getOrNull() ?: emptyList()
            val sleeps = listSleep(from, to).getOrNull() ?: emptyList()
            val diapers = listDiaper(from, to).getOrNull() ?: emptyList()

            val sleepByDay = dayNames.associateWith { 0L }.toMutableMap()
            sleeps.forEach { s ->
                val day = ZonedDateTime.ofInstant(s.startTime, ZoneId.systemDefault())
                val name = dayNames[(day.dayOfWeek.value - 1) % 7]
                sleepByDay[name] = (sleepByDay[name] ?: 0L) + s.durationMinutes
            }

            _state.value = InsightsState(
                feedCount = feeds.size,
                sleepCount = sleeps.size,
                diaperCount = diapers.size,
                sleepMinutesByDay = sleepByDay,
            )
        }
    }
}
