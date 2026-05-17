package com.babyvault.android.presentation.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        HistoryHeader(rangeDays = rangeDays, onRangeChange = { rangeDays = it })
        HorizontalDivider()

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal500)
            }
            state.items.isEmpty() -> EmptyState()
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.items, key = { it.id }) { HistoryItemCard(it) }
            }
        }
    }
}

@Composable
private fun HistoryHeader(rangeDays: Int, onRangeChange: (Int) -> Unit) {
    val options = listOf(1 to "Today", 7 to "7 days", 30 to "30 days")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text("History", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { (days, label) ->
                FilterChip(selected = rangeDays == days, onClick = { onRangeChange(days) }, label = { Text(label) })
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = CircleShape, color = Teal100, modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Inbox, null, tint = Teal500, modifier = Modifier.size(36.dp))
                }
            }
            Text("No activity yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            Text("Tap a category on the home screen to log something", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
