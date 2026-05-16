package com.babyvault.android.presentation.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSleepScreen(onBack: () -> Unit, viewModel: LogSleepViewModel = hiltViewModel()) {
    val saved by viewModel.saved.collectAsState()
    LaunchedEffect(saved) { if (saved) onBack() }

    var durationHours by remember { mutableStateOf("") }
    var durationMins by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Sleep") },
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
            Text("Duration", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = durationHours,
                    onValueChange = { durationHours = it },
                    label = { Text("Hours") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = durationMins,
                    onValueChange = { durationMins = it },
                    label = { Text("Minutes") },
                    modifier = Modifier.weight(1f),
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
                    val totalMins = ((durationHours.toIntOrNull() ?: 0) * 60) + (durationMins.toIntOrNull() ?: 0)
                    val endMillis = System.currentTimeMillis()
                    val startMillis = endMillis - (totalMins * 60_000L)
                    viewModel.save(startMillis, endMillis, notes)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
        }
    }
}
