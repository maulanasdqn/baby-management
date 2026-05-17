package com.babyvault.android.presentation.screens.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvault.android.core.vault.VaultEngineProvider
import com.babyvault.android.data.local.BabyProfileStore
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.domain.usecase.ListDiaperByRangeUseCase
import com.babyvault.android.domain.usecase.ListFeedByRangeUseCase
import com.babyvault.android.domain.usecase.ListMilestonesUseCase
import com.babyvault.android.domain.usecase.ListSleepByRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
data class HomeUiState(
    val engineVersion: String = "",
    val milestones: List<Milestone> = emptyList(),
    val isLoading: Boolean = true,
    val babyName: String = "",
    val babyAgeLabel: String = "",
    val todayFeeds: Int = 0,
    val todaySleepMinutes: Long = 0L,
    val todayDiapers: Int = 0,
)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val listMilestones: ListMilestonesUseCase,
    private val engineProvider: VaultEngineProvider,
    private val profileStore: BabyProfileStore,
    private val listFeed: ListFeedByRangeUseCase,
    private val listSleep: ListSleepByRangeUseCase,
    private val listDiaper: ListDiaperByRangeUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    init {
        viewModelScope.launch { load() }
    }
    private suspend fun load() {
        val version = engineProvider.engine.engineVersion()
        val milestones = listMilestones(limit = 5).getOrDefault(emptyList())
        val profile = profileStore.profile.first()
        val babyName = profile?.name ?: ""
        val babyAgeLabel = profile?.let { ageLabel(it.dobMillis) } ?: ""
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        val todayFeeds = listFeed(todayStart, now).getOrNull()?.size ?: 0
        val todaySleepMinutes = listSleep(todayStart, now).getOrNull()
            ?.sumOf { it.durationMinutes } ?: 0L
        val todayDiapers = listDiaper(todayStart, now).getOrNull()?.size ?: 0
        _state.value = HomeUiState(
            engineVersion = version,
            milestones = milestones,
            isLoading = false,
            babyName = babyName,
            babyAgeLabel = babyAgeLabel,
            todayFeeds = todayFeeds,
            todaySleepMinutes = todaySleepMinutes,
            todayDiapers = todayDiapers,
        )
    }
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            load()
        }
    }
    private fun ageLabel(dobMillis: Long): String {
        val dob = Instant.ofEpochMilli(dobMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val now = LocalDate.now()
        val months = ChronoUnit.MONTHS.between(dob, now).toInt()
        return when {
            months < 1 -> "newborn"
            months < 12 -> "$months month${if (months == 1) "" else "s"} old"
            else -> {
                val years = months / 12
                val rem = months % 12
                if (rem == 0) "$years year${if (years == 1) "" else "s"} old"
                else "$years yr $rem mo old"
            }
        }
    }
}
