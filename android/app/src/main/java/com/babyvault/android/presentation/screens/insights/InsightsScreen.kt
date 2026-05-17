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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.presentation.theme.*

@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                "Insights",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                "Last 7 days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Summary row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("🍼", "Feeds", "${state.feedCount}", MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
                StatCard("😴", "Sleeps", "${state.sleepCount}", MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
                StatCard("🚼", "Diapers", "${state.diaperCount}", MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
            }

            // Average sleep card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Avg sleep / day",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val avg = if (state.sleepMinutesByDay.isNotEmpty()) {
                        state.sleepMinutesByDay.values.filter { it > 0 }.let { l ->
                            if (l.isEmpty()) 0L else l.average().toLong()
                        }
                    } else 0L
                    Text(
                        if (avg == 0L) "No data" else "${avg / 60}h ${avg % 60}m",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Sleep bar chart
            Column {
                Text(
                    "Sleep duration (this week)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(12.dp))
                SleepBarChart(state.sleepMinutesByDay)
            }

            // Feed type breakdown
            if (state.feedCount > 0) {
                Column {
                    Text(
                        "Feed breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FeedTypeChip("Breast", state.breastFeeds, Peach100, Peach400, Modifier.weight(1f))
                        FeedTypeChip("Bottle", state.bottleFeeds, SkyBlue100, SkyBlue400, Modifier.weight(1f))
                        FeedTypeChip("Solid", state.solidFeeds, Mint100, Mint400, Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(emoji: String, label: String, value: String, bg: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun FeedTypeChip(label: String, count: Int, bg: Color, accent: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$count", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = accent)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SleepBarChart(sleepMinutesByDay: Map<String, Long>) {
    val maxVal = sleepMinutesByDay.values.maxOrNull()?.takeIf { it > 0 } ?: 1L
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEach { day ->
                val mins = sleepMinutesByDay[day] ?: 0L
                val frac = mins.toFloat() / maxVal.toFloat()
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    if (mins > 0) {
                        val h = mins / 60
                        val m = mins % 60
                        Text(
                            if (h > 0) "${h}h" else "${m}m",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height((100 * frac).dp.coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (mins > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                            ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(day, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
