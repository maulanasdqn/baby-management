package com.babyvault.android.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.presentation.theme.Amber100
import com.babyvault.android.presentation.theme.Amber400
import com.babyvault.android.presentation.theme.CardWhite
import com.babyvault.android.presentation.theme.Indigo100
import com.babyvault.android.presentation.theme.Indigo400
import com.babyvault.android.presentation.theme.Lavender100
import com.babyvault.android.presentation.theme.Lavender400
import com.babyvault.android.presentation.theme.NavyPrimary
import com.babyvault.android.presentation.theme.Navy900
import com.babyvault.android.presentation.theme.PinkBlob
import com.babyvault.android.presentation.theme.PinkLight
import com.babyvault.android.presentation.theme.Rose100
import com.babyvault.android.presentation.theme.Rose400
import com.babyvault.android.presentation.theme.Sage100
import com.babyvault.android.presentation.theme.Sage400
import com.babyvault.android.presentation.theme.SkyBlue100
import com.babyvault.android.presentation.theme.SkyBlue400
import com.babyvault.android.presentation.theme.Teal100
import com.babyvault.android.presentation.theme.Teal500
import com.babyvault.android.presentation.theme.TextPrimary
import com.babyvault.android.presentation.theme.TextSecondary
import com.babyvault.android.presentation.theme.WarmCream
import com.babyvault.android.presentation.utils.formatSleepMinutes

private data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val bgColor: Color,
    val iconColor: Color,
    val route: String,
)

private val quickActions = listOf(
    QuickAction(Icons.Filled.LocalDrink,             "Feed",      Rose100,     Rose400,     "log_feed"),
    QuickAction(Icons.Filled.Timer,                  "Timer",     Amber100,    Amber400,    "feed_timer"),
    QuickAction(Icons.Filled.Bedtime,                "Sleep",     SkyBlue100,  SkyBlue400,  "log_sleep"),
    QuickAction(Icons.Filled.ChildCare,              "Diaper",    Sage100,     Sage400,     "log_diaper"),
    QuickAction(Icons.Filled.Star,                   "Milestone", Teal100,     Teal500,     "log_milestone"),
    QuickAction(Icons.AutoMirrored.Filled.ShowChart, "Growth",    Lavender100, Lavender400, "log_growth"),
    QuickAction(Icons.Filled.PhotoCamera,            "Media",     Indigo100,   Indigo400,   "media"),
)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState()),
    ) {
        HomeHeader(name = state.babyName, ageLabel = state.babyAgeLabel)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TodayStat(Icons.Filled.LocalDrink, Rose100, Rose400, "${state.todayFeeds}", "Feeds", Modifier.weight(1f))
                TodayStat(Icons.Filled.Bedtime, SkyBlue100, SkyBlue400, formatSleepMinutes(state.todaySleepMinutes), "Sleep", Modifier.weight(1f))
                TodayStat(Icons.Filled.ChildCare, Sage100, Sage400, "${state.todayDiapers}", "Diapers", Modifier.weight(1f))
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Quick Log",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )

            Spacer(Modifier.height(12.dp))

            // Fixed-height 2-column grid (no nested scrolling needed)
            val rows = (quickActions.size + 1) / 2
            val gridHeight = (rows * 90 + (rows - 1) * 10).dp
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(gridHeight),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false,
            ) {
                items(quickActions) { action ->
                    QuickActionCard(action = action, onClick = { onNavigate(action.route) })
                }
            }

            if (state.milestones.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                MilestoneSection(milestones = state.milestones.take(3))
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun HomeHeader(name: String, ageLabel: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Brush.verticalGradient(listOf(Navy900, NavyPrimary))),
    ) {
        // Large decorative blob — top-right
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 60.dp, y = (-40).dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(PinkBlob.copy(alpha = 0.45f)),
        )
        // Smaller blob — bottom-left
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = (-30).dp, y = 30.dp)
                .align(Alignment.BottomStart)
                .clip(CircleShape)
                .background(PinkBlob.copy(alpha = 0.25f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Good morning 👋",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = PinkBlob.copy(alpha = 0.35f), modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👶", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Column {
                    Text(
                        if (name.isNotBlank()) name else "Baby Vault",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    if (ageLabel.isNotBlank()) {
                        Text(ageLabel, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.68f))
                    }
                }
                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun TodayStat(
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(bgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp), color = TextPrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun QuickActionCard(action: QuickAction, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().height(88.dp).clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(action.bgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(action.icon, contentDescription = null, tint = action.iconColor, modifier = Modifier.size(22.dp))
            }
            Text(
                action.label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun MilestoneSection(milestones: List<Milestone>) {
    Column {
        Text(
            "Recent Milestones",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
        )
        Spacer(Modifier.height(12.dp))
        milestones.forEachIndexed { index, milestone ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (index == 0) NavyPrimary else PinkBlob))
                    if (index < milestones.size - 1) {
                        Box(modifier = Modifier.width(2.dp).height(52.dp).background(Color(0xFFE0E2F0)))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardWhite,
                    shadowElevation = 1.dp,
                    modifier = Modifier.weight(1f).padding(bottom = if (index < milestones.size - 1) 8.dp else 0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(PinkLight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Star, null, tint = Rose400, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(milestone.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                            if (milestone.description.isNotBlank()) {
                                Text(milestone.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
