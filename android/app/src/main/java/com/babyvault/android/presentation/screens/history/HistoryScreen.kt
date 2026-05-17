package com.babyvault.android.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.presentation.theme.*

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var rangeDays by remember { mutableIntStateOf(7) }
    LaunchedEffect(rangeDays) { viewModel.loadRange(rangeDays) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with statusBarsPadding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                "History",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val options = listOf(1 to "Today", 7 to "7 days", 30 to "30 days")
                items(options) { (days, label) ->
                    FilterChip(
                        selected = rangeDays == days,
                        onClick = { rangeDays = days },
                        label = { Text(label) },
                    )
                }
            }
        }

        HorizontalDivider()

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal500)
            }
        } else if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Teal100,
                        modifier = Modifier.size(72.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Inbox,
                                contentDescription = null,
                                tint = Teal500,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }
                    Text(
                        "No activity yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                    )
                    Text(
                        "Tap a category on the home screen to log something",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    HistoryItemCard(item)
                }
            }
        }
    }
}

private data class ItemDecor(val icon: ImageVector, val iconBg: Color, val iconTint: Color)

private fun itemDecorFor(id: String): ItemDecor = when {
    id.startsWith("feed_") -> ItemDecor(Icons.Filled.LocalDrink, Peach100, Peach400)
    id.startsWith("sleep_") -> ItemDecor(Icons.Filled.Bedtime, SkyBlue100, SkyBlue400)
    id.startsWith("diaper_") -> ItemDecor(Icons.Filled.ChildCare, Mint100, Mint400)
    id.startsWith("milestone_") -> ItemDecor(Icons.Filled.Star, Teal100, Teal500)
    id.startsWith("growth_") -> ItemDecor(Icons.Filled.ShowChart, Lavender100, Lavender400)
    else -> ItemDecor(Icons.Filled.Circle, Color(0xFFE8E8E8), Color(0xFFAAAAAA))
}

@Composable
private fun HistoryItemCard(item: HistoryItem) {
    val decor = itemDecorFor(item.id)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = decor.iconBg,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        decor.icon,
                        contentDescription = null,
                        tint = decor.iconTint,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                )
                if (item.subtitle.isNotBlank()) {
                    Text(
                        item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                    )
                }
            }
            Text(
                item.timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}
