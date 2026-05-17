package com.babyvault.android.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.presentation.theme.CardSurface
import com.babyvault.android.presentation.theme.NeutralGray
import com.babyvault.android.presentation.theme.Teal100
import com.babyvault.android.presentation.theme.Teal400
import com.babyvault.android.presentation.theme.Teal500
import com.babyvault.android.presentation.theme.Teal600
import com.babyvault.android.presentation.theme.TextPrimary
import com.babyvault.android.presentation.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DOB_FMT = DateTimeFormatter.ofPattern("d MMMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now().minusMonths(3)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dirty by remember { mutableStateOf(false) }

    // Populate fields once profile loads
    LaunchedEffect(state.profile) {
        state.profile?.let { p ->
            if (!dirty) {
                name = p.name
                selectedDate = Instant.ofEpochMilli(p.dobMillis).atZone(ZoneOffset.UTC).toLocalDate()
            }
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbar.showSnackbar("Profile saved")
            dirty = false
            viewModel.resetSaved()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / 86_400_000)
                        dirty = true
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralGray)
                .verticalScroll(rememberScrollState()),
        ) {
            // Gradient header with avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.verticalGradient(listOf(Teal600, Teal400))),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 32.dp),
                ) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(80.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👶", style = MaterialTheme.typography.displaySmall)
                        }
                    }
                    if (name.isNotBlank()) {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                    val age = Period.between(selectedDate, LocalDate.now())
                    val ageLabel = when {
                        age.years > 0 -> "${age.years}y ${age.months}m old"
                        age.months > 0 -> "${age.months} months old"
                        else -> "${age.days} days old"
                    }
                    Text(ageLabel, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Name field
                Surface(shape = RoundedCornerShape(20.dp), color = CardSurface, shadowElevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "BABY'S NAME",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; dirty = true },
                            placeholder = { Text("Enter name") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Teal500,
                                unfocusedBorderColor = NeutralGray,
                                focusedContainerColor = NeutralGray,
                                unfocusedContainerColor = NeutralGray,
                            ),
                        )
                    }
                }

                // Date of birth field
                Surface(shape = RoundedCornerShape(20.dp), color = CardSurface, shadowElevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "DATE OF BIRTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NeutralGray, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(Icons.Default.Cake, contentDescription = null, tint = Teal500, modifier = Modifier.size(20.dp))
                                Text(DOB_FMT.format(selectedDate), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                            IconButton(onClick = { showDatePicker = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.EditCalendar, contentDescription = "Change date", tint = Teal500, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = {
                        val dob = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                        viewModel.save(name, dob)
                    },
                    enabled = name.isNotBlank() && dirty,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Save Changes", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                }
            }
        }
    }
}
