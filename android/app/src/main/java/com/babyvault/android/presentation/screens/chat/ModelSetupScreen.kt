package com.babyvault.android.presentation.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSetupScreen(
    onBack: () -> Unit,
    viewModel: ModelSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isReady) {
        if (state.isReady) onBack()
    }

    Scaffold(
        containerColor = NeutralGray,
        topBar = {
            TopAppBar(
                title = { Text("AI Setup", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralGray),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            Surface(shape = CircleShape, color = Teal100, modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CloudDownload, null, tint = Teal600, modifier = Modifier.size(40.dp))
                }
            }

            Text(
                "Gemma 3 1B",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            Text(
                "Download the on-device AI model (~2.3 GB).\nYour data stays private — inference runs entirely on your phone.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            when {
                state.isDownloading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Teal500,
                            trackColor = Teal100,
                        )
                        Text(
                            "${(state.progress * 100).toInt()}% — ${state.statusText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
                state.error != null -> {
                    Text(state.error!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    Button(
                        onClick = { viewModel.startDownload() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Text("Retry") }
                }
                else -> {
                    Button(
                        onClick = { viewModel.startDownload() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Text("Download Model") }
                }
            }

            Text(
                "Requires Wi-Fi recommended. Model stored in app-private storage.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
