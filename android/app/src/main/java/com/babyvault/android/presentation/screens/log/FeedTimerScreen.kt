package com.babyvault.android.presentation.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedTimerScreen(onBack: () -> Unit, viewModel: FeedTimerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.saved.collect { onBack() } }

    Scaffold(
        containerColor = NeutralGray,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Feed Timer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralGray,
                    scrolledContainerColor = NeutralGray,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        ) {
            // Circular timer display in Peach tones
            Surface(
                shape = CircleShape,
                color = Peach100,
                modifier = Modifier.size(220.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        state.elapsedLabel,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Peach400,
                    )
                }
            }

            // Side selector
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Side",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Left", "Right", "Both").forEach { side ->
                        FilterChip(
                            selected = state.side == side,
                            onClick = { viewModel.setSide(side) },
                            label = { Text(side) },
                        )
                    }
                }
            }

            // Playback controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!state.isRunning) {
                    Surface(
                        onClick = { viewModel.start() },
                        shape = CircleShape,
                        color = Peach100,
                        modifier = Modifier.size(64.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = Peach400,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = { viewModel.pause() },
                        shape = CircleShape,
                        color = Peach100,
                        modifier = Modifier.size(64.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = Peach400,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
                if (state.elapsedSeconds > 0) {
                    Surface(
                        onClick = { viewModel.stop() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(64.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
            }

            Text(
                "Tap stop to save the session",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
