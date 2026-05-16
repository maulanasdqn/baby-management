package com.babyvault.android.presentation.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Insights", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        Text("This week", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Feeds", state.feedCount.toString(), MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
            SummaryCard("Sleeps", state.sleepCount.toString(), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
            SummaryCard("Diapers", state.diaperCount.toString(), MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Text("Sleep duration (last 7 days)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        SleepBarChart(state.sleepMinutesByDay)

        Spacer(Modifier.height(20.dp))

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Avg sleep / day", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val avg = if (state.sleepCount > 0) state.sleepMinutesByDay.values.average().toLong() else 0L
                Text("${avg / 60}h ${avg % 60}m", style = MaterialTheme.typography.headlineMedium)
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SummaryCard(label: String, value: String, containerColor: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SleepBarChart(sleepMinutesByDay: Map<String, Long>) {
    val maxVal = sleepMinutesByDay.values.maxOrNull()?.takeIf { it > 0 } ?: 1L
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { day ->
            val mins = sleepMinutesByDay[day] ?: 0L
            val frac = mins.toFloat() / maxVal.toFloat()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height((100 * frac).dp.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Spacer(Modifier.height(4.dp))
                Text(day, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
