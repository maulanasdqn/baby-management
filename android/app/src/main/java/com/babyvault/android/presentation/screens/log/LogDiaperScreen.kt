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
import com.babyvault.android.domain.model.DiaperType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDiaperScreen(onBack: () -> Unit, viewModel: LogDiaperViewModel = hiltViewModel()) {
    val saved by viewModel.saved.collectAsState()
    LaunchedEffect(saved) { if (saved) onBack() }

    var diaperType by remember { mutableStateOf(DiaperType.WET) }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Diaper") },
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
                DiaperType.entries.forEach { type ->
                    FilterChip(
                        selected = diaperType == type,
                        onClick = { diaperType = type },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { viewModel.save(diaperType, notes) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
        }
    }
}
