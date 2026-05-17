package com.babyvault.android.presentation.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SmartToy
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
    LaunchedEffect(Unit) { viewModel.check() }
    LaunchedEffect(state.isReady) { if (state.isReady) onBack() }

    Scaffold(
        containerColor = NeutralGray,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI Setup",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
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
                    Icon(Icons.Default.SmartToy, null, tint = Teal600, modifier = Modifier.size(40.dp))
                }
            }

            Text(
                "Baby AI — Rust Engine",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )

            Text(
                "On-device language model powered by Burn inference.\nRuns fully offline — your data never leaves the device.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            when {
                state.isDownloading -> DownloadProgress(state)
                state.isLoading -> LoadingModel()
                state.error != null -> ErrorCard(
                    message = state.error!!,
                    onRetry = {
                        if (state.isModelPresent) viewModel.check() else viewModel.download()
                    },
                    onBack = onBack,
                )
                state.isModelPresent -> LoadingModel()
                else -> DownloadPrompt(onDownload = { viewModel.download() }, onSkip = onBack)
            }
        }
    }
}

@Composable
private fun DownloadProgress(state: ModelSetupState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LinearProgressIndicator(
            progress = { state.downloadProgress },
            modifier = Modifier.fillMaxWidth(),
            color = Teal500,
        )
        Text(
            "${state.downloadFile}  ${(state.downloadProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Text(
            "GPT-2 model (~550 MB). Keep screen on.",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}

@Composable
private fun LoadingModel() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(color = Teal500)
        Text("Loading model into memory…", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Error",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
    Button(
        onClick = onRetry,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Teal500),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) { Text("Retry") }
    OutlinedButton(
        onClick = onBack,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) { Text("Use Basic Mode") }
}

@Composable
private fun DownloadPrompt(onDownload: () -> Unit, onSkip: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Model not downloaded", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            Text("GPT-2 (124M params) — ~550 MB download required for AI responses.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
    Button(
        onClick = onDownload,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Teal500),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Download Model")
    }
    OutlinedButton(
        onClick = onSkip,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) { Text("Use Basic Mode") }
}
