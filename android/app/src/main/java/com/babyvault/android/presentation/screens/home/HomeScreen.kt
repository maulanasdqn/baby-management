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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.presentation.theme.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Teal600, Teal500, Teal400))
                )
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        if (state.babyName.isNotBlank()) "Hi, ${state.babyName}" else "Baby Vault",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    if (state.babyAgeLabel.isNotBlank()) {
                        Text(
                            state.babyAgeLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.80f),
                        )
                    }
                }
                IconButton(onClick = { onNavigate("settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TodayStat(Icons.Filled.LocalDrink, Peach100, Peach400, "${state.todayFeeds}", "feeds")
            VerticalDivider(modifier = Modifier.height(36.dp))
            TodayStat(Icons.Filled.Bedtime, SkyBlue100, SkyBlue400, formatSleepMinutes(state.todaySleepMinutes), "sleep")
            VerticalDivider(modifier = Modifier.height(36.dp))
            TodayStat(Icons.Filled.ChildCare, Mint100, Mint400, "${state.todayDiapers}", "diapers")
        }

        Spacer(Modifier.height(20.dp))
        
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Quick Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CategoryCard(Icons.Filled.LocalDrink, "Feed", Peach100, Peach400) { onNavigate("log_feed") }
            CategoryCard(Icons.Filled.Timer, "Timer", Peach100, Peach400) { onNavigate("feed_timer") }
            CategoryCard(Icons.Filled.Bedtime, "Sleep", SkyBlue100, SkyBlue400) { onNavigate("log_sleep") }
            CategoryCard(Icons.Filled.ChildCare, "Diaper", Mint100, Mint400) { onNavigate("log_diaper") }
            CategoryCard(Icons.Filled.Star, "Milestone", Teal100, Teal500) { onNavigate("log_milestone") }
            CategoryCard(Icons.AutoMirrored.Filled.ShowChart, "Growth", Lavender100, Lavender400) { onNavigate("log_growth") }
            CategoryCard(Icons.Filled.PhotoCamera, "Media", Peach100, Peach400) { onNavigate("media") }
        }

        Spacer(Modifier.height(24.dp))
        
        if (state.milestones.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Latest Milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                state.milestones.take(3).forEach { m ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Teal100,
                                modifier = Modifier.size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Teal500,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Column {
                                Text(m.title, style = MaterialTheme.typography.titleSmall)
                                if (m.description.isNotBlank()) {
                                    Text(
                                        m.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun formatSleepMinutes(minutes: Long): String {
    if (minutes == 0L) return "—"
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h${if (m > 0) " ${m}m" else ""}" else "${m}m"
}

@Composable
private fun TodayStat(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = iconBg,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CategoryCard(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 88.dp, height = 88.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
