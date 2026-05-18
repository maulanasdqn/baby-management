package com.babyvault.android.presentation.screens.growth
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.domain.model.GrowthLog
import java.time.ZoneId
import java.time.format.DateTimeFormatter
private val DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneId.systemDefault())
private val AXIS_FMT = DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault())
@Composable
fun GrowthScreen(viewModel: GrowthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("Log Growth", style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = { weight = it },
                                    label = { Text("Weight (g)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )
                                OutlinedTextField(
                                    value = height,
                                    onValueChange = { height = it },
                                    label = { Text("Height (mm)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )
                            }
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Notes (optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                            )
                            Button(
                                onClick = {
                                    viewModel.log(weight, height, notes)
                                    weight = ""; height = ""; notes = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Log") }
                        }
                    }
                }

                val weightLogs = state.logs.filter { it.weightGrams != null }.sortedBy { it.loggedAt }
                if (weightLogs.size >= 2) {
                    item {
                        WeightChart(logs = weightLogs)
                    }
                }

                item {
                    Text("History", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 4.dp))
                }
                if (state.logs.isEmpty()) {
                    item {
                        Text(
                            "No growth logs yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.logs, key = { it.id }) { log ->
                        GrowthLogCard(log, onDelete = { viewModel.delete(log.id) })
                    }
                }
                state.error?.let {
                    item {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightChart(logs: List<GrowthLog>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Weight Over Time", style = MaterialTheme.typography.titleSmall)
            val minW = logs.minOf { it.weightGrams!! }.toFloat()
            val maxW = logs.maxOf { it.weightGrams!! }.toFloat()
            val minT = logs.first().loggedAt.toEpochMilli().toFloat()
            val maxT = logs.last().loggedAt.toEpochMilli().toFloat()
            val lineColor = Color(0xFF4DB6AC)
            val dotColor = Color(0xFF00897B)
            Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                val w = size.width
                val h = size.height
                val pad = 24f
                val xRange = (maxT - minT).coerceAtLeast(1f)
                val yRange = (maxW - minW).coerceAtLeast(1f)
                val points = logs.map { log ->
                    Offset(
                        pad + ((log.loggedAt.toEpochMilli() - minT) / xRange) * (w - 2 * pad),
                        h - pad - ((log.weightGrams!! - minW) / yRange) * (h - 2 * pad),
                    )
                }
                val path = Path()
                points.forEachIndexed { i, pt ->
                    if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }
                drawPath(path, color = lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                points.forEach { pt ->
                    drawCircle(color = dotColor, radius = 6f, center = pt)
                    drawCircle(color = Color.White, radius = 3f, center = pt)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(AXIS_FMT.format(logs.first().loggedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(AXIS_FMT.format(logs.last().loggedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun GrowthLogCard(log: GrowthLog, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    DATE_FMT.format(log.loggedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    log.weightGrams?.let { Text("${it}g", style = MaterialTheme.typography.bodyMedium) }
                    log.heightMm?.let { Text("${it}mm", style = MaterialTheme.typography.bodyMedium) }
                }
                if (log.notes.isNotBlank()) {
                    Text(log.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFB0B0B0), modifier = Modifier.size(18.dp))
            }
        }
    }
}
