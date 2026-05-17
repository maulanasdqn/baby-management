package com.babyvault.android.presentation.screens.insights
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.presentation.theme.*
@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        InsightsHeader()
        HorizontalDivider()
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InsightStatCard(Icons.Filled.LocalDrink, Rose100,    Rose400,    "Feeds",   "${state.feedCount}",   Modifier.weight(1f))
                InsightStatCard(Icons.Filled.Bedtime,    SkyBlue100, SkyBlue400, "Sleeps",  "${state.sleepCount}",  Modifier.weight(1f))
                InsightStatCard(Icons.Filled.ChildCare,  Sage100,    Sage400,    "Diapers", "${state.diaperCount}", Modifier.weight(1f))
            }
            AvgSleepCard(label = state.avgSleepLabel)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Sleep this week", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                SleepBarChart(state.sleepMinutesByDay)
            }
            if (state.feedCount > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Feed breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FeedTypeCard("Breast", state.breastFeeds, Rose100,    Rose400,    Modifier.weight(1f))
                        FeedTypeCard("Bottle", state.bottleFeeds, SkyBlue100, SkyBlue400, Modifier.weight(1f))
                        FeedTypeCard("Solid",  state.solidFeeds,  Sage100,    Sage400,    Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
@Composable
private fun InsightsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text("Insights", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Text("Last 7 days", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}
@Composable
private fun AvgSleepCard(label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralGray),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Avg sleep / day", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(label, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Teal600)
        }
    }
}
