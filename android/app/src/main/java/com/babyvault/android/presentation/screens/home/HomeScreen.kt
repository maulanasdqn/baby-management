package com.babyvault.android.presentation.screens.home
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.presentation.theme.*
import com.babyvault.android.presentation.utils.formatSleepMinutes
@Composable
fun HomeScreen(onNavigate: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardSurface)
            .verticalScroll(rememberScrollState()),
    ) {
        HomeHeader(name = state.babyName, ageLabel = state.babyAgeLabel, onSettings = { onNavigate("settings") })
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TodayStatCard(Icons.Filled.LocalDrink, Rose100, Rose400, "${state.todayFeeds}", "Feeds", Modifier.weight(1f))
            TodayStatCard(Icons.Filled.Bedtime, SkyBlue100, SkyBlue400, formatSleepMinutes(state.todaySleepMinutes), "Sleep", Modifier.weight(1f))
            TodayStatCard(Icons.Filled.ChildCare, Sage100, Sage400, "${state.todayDiapers}", "Diapers", Modifier.weight(1f))
        }
        Text(
            "Quick Log",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CategoryCard(Icons.Filled.LocalDrink,              "Feed",      Rose100,     Rose400)     { onNavigate("log_feed") }
            CategoryCard(Icons.Filled.Timer,                   "Timer",     Amber100,    Amber400)    { onNavigate("feed_timer") }
            CategoryCard(Icons.Filled.Bedtime,                 "Sleep",     SkyBlue100,  SkyBlue400)  { onNavigate("log_sleep") }
            CategoryCard(Icons.Filled.ChildCare,               "Diaper",    Sage100,     Sage400)     { onNavigate("log_diaper") }
            CategoryCard(Icons.Filled.Star,                    "Milestone", Teal100,     Teal500)     { onNavigate("log_milestone") }
            CategoryCard(Icons.AutoMirrored.Filled.ShowChart,  "Growth",    Lavender100, Lavender400) { onNavigate("log_growth") }
            CategoryCard(Icons.Filled.PhotoCamera,             "Media",     Indigo100,   Indigo400)   { onNavigate("media") }
        }
        if (state.milestones.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            MilestoneSection(milestones = state.milestones.take(3))
        }
        Spacer(Modifier.height(20.dp))
    }
}
@Composable
private fun HomeHeader(name: String, ageLabel: String, onSettings: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Teal700, Teal500)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (name.isNotBlank()) "Hi, $name" else "Baby Vault",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                if (ageLabel.isNotBlank()) {
                    Text(ageLabel, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.75f))
                }
            }
            Surface(onClick = onSettings, shape = CircleShape, color = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
@Composable
private fun MilestoneSection(milestones: List<com.babyvault.android.domain.model.Milestone>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Latest Milestones", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Spacer(Modifier.height(10.dp))
        milestones.forEach { m ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralGray),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Teal100), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Star, null, tint = Teal500, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(m.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        if (m.description.isNotBlank()) {
                            Text(m.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
