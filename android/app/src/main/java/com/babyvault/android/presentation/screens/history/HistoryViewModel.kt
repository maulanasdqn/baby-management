package com.babyvault.android.presentation.screens.history
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.domain.usecase.ListDiaperByRangeUseCase
import com.babyvault.android.domain.usecase.ListFeedByRangeUseCase
import com.babyvault.android.domain.usecase.ListMilestonesUseCase
import com.babyvault.android.domain.usecase.ListSleepByRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
data class HistoryItem(val id: String, val label: String, val subtitle: String, val timeLabel: String)
data class HistoryState(val items: List<HistoryItem> = emptyList(), val isLoading: Boolean = false)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val listFeed: ListFeedByRangeUseCase,
    private val listSleep: ListSleepByRangeUseCase,
    private val listDiaper: ListDiaperByRangeUseCase,
    private val listMilestones: ListMilestonesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryState(isLoading = true))
    val state: StateFlow<HistoryState> = _state.asStateFlow()
    private val fmt = DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault())
    init {
        loadRange(7)
    }
    fun loadRange(days: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val to = System.currentTimeMillis()
            val from = to - (days.toLong() * 24 * 60 * 60 * 1000)
            loadInternal(from, to)
        }
    }
    private suspend fun loadInternal(from: Long, to: Long) {
        val items = mutableListOf<HistoryItem>()
        listFeed(from, to).getOrNull()?.forEach { f ->
            items += HistoryItem(
                id = "feed_${f.id}",
                label = "Feed — ${f.feedType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                subtitle = listOfNotNull(
                    f.amountMl?.let { "${it}ml" },
                    f.durationMinutes?.let { "${it}min" },
                    f.side,
                ).joinToString(", ").ifEmpty { f.notes },
                timeLabel = fmt.format(f.loggedAt),
            )
        }
        listSleep(from, to).getOrNull()?.forEach { s ->
            val hours = s.durationMinutes / 60
            val mins = s.durationMinutes % 60
            items += HistoryItem(
                id = "sleep_${s.id}",
                label = "Sleep — ${hours}h ${mins}m",
                subtitle = s.notes,
                timeLabel = fmt.format(s.startTime),
            )
        }
        listDiaper(from, to).getOrNull()?.forEach { d ->
            items += HistoryItem(
                id = "diaper_${d.id}",
                label = "Diaper — ${d.diaperType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                subtitle = d.notes,
                timeLabel = fmt.format(d.loggedAt),
            )
        }
        listMilestones(20, 0).getOrNull()?.forEach { m ->
            items += HistoryItem(
                id = "milestone_${m.id}",
                label = "Milestone — ${m.title}",
                subtitle = m.description,
                timeLabel = fmt.format(m.occurredAt),
            )
        }
        items.sortByDescending { it.timeLabel }
        _state.value = HistoryState(items = items, isLoading = false)
    }
}
