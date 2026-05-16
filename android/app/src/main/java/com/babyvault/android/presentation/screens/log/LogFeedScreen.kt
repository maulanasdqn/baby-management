package com.babyvault.android.presentation.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.domain.model.FeedType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFeedScreen(onBack: () -> Unit, viewModel: LogFeedViewModel = hiltViewModel()) {
    val saved by viewModel.saved.collectAsState()
    LaunchedEffect(saved) { if (saved) onBack() }

    var feedType by remember { mutableStateOf(FeedType.BREAST) }
    var amountMl by remember { mutableStateOf("") }
    var durationMin by remember { mutableStateOf("") }
    var side by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Feed") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Type", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeedType.entries.forEach { type ->
                    FilterChip(
                        selected = feedType == type,
                        onClick = { feedType = type },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            if (feedType == FeedType.BOTTLE) {
                OutlinedTextField(
                    value = amountMl,
                    onValueChange = { amountMl = it },
                    label = { Text("Amount (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (feedType == FeedType.BREAST) {
                OutlinedTextField(
                    value = durationMin,
                    onValueChange = { durationMin = it },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = side,
                    onValueChange = { side = it },
                    label = { Text("Side (Left / Right / Both)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    viewModel.save(
                        feedType,
                        amountMl.toIntOrNull(),
                        durationMin.toIntOrNull(),
                        side.takeIf { it.isNotBlank() },
                        notes,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
        }
    }
}
